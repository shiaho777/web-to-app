/*
 * static_exec_loader — user-mode exec for static ELFs under SELinux W^X.
 *
 * targetSdk>=29 apps cannot exec (or exec-map) files in app storage, so the
 * pmmp static PHP binary can never go through execve(2). This loader rebuilds
 * what the kernel does for execve, entirely in user mode:
 *
 *   1. fork a clean child (raw syscall; no atfork handlers run)
 *   2. map the ELF's PT_LOAD segments from an executable memfd — the same
 *      permission class ART JIT and the patched-musl bridge (#590) ride on;
 *      direct exec-maps of app_data are what W^X denies
 *   3. build the initial stack (argc/argv/envp/auxv) per the AArch64 ELF ABI
 *   4. jump to e_entry
 *
 * Everything after fork() uses only raw syscalls plus lock-free libc string
 * helpers writing into freshly mmap'd regions — async-signal-safe by
 * construction. ELF parsing, the memfd copy, and argv/envp blob assembly all
 * happen in the parent before the fork.
 *
 * Host preview only: exported APKs (targetSdk 28) keep plain execve.
 */
#ifndef WTA_CLI_HARNESS

#include <jni.h>
#include <errno.h>
#include <fcntl.h>
#include <signal.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/mman.h>
#include <sys/syscall.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <unistd.h>

#else /* WTA_CLI_HARNESS */

#include <errno.h>
#include <fcntl.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/mman.h>
#include <unistd.h>

#endif

#define WTA_PAGE 4096UL
#define WTA_MAX_PHDR 64
#define WTA_STACK_SIZE (8UL << 20)

#define PT_LOAD      1
#define PT_TLS       7
#define PT_GNU_RELRO 0x6474e552U

typedef struct {
    uint32_t p_type;
    uint32_t p_flags;
    uint64_t p_offset;
    uint64_t p_vaddr;
    uint64_t p_paddr;
    uint64_t p_filesz;
    uint64_t p_memsz;
    uint64_t p_align;
} WtaPhdr;

typedef struct {
    uint8_t  e_ident[16];
    uint16_t e_type;
    uint16_t e_machine;
    uint32_t e_version;
    uint64_t e_entry;
    uint64_t e_phoff;
    uint64_t e_shoff;
    uint32_t e_flags;
    uint16_t e_ehsize;
    uint16_t e_phentsize;
    uint16_t e_phnum;
    uint16_t e_shentsize;
    uint16_t e_shnum;
    uint16_t e_shstrndx;
} WtaEhdr;

typedef struct {
    int      ok;
    char     err[160];
    uint64_t entry;
    uint64_t phoff;
    uint16_t phentsize;
    uint16_t phnum;
    WtaPhdr  ph[WTA_MAX_PHDR];
    uint64_t map_start;
    uint64_t map_end;
    int      has_tls;
} WtaElf;

static uint64_t wta_round_down(uint64_t v, uint64_t a) { return v & ~(a - 1); }
static uint64_t wta_round_up(uint64_t v, uint64_t a) { return (v + a - 1) & ~(a - 1); }

static int wta_parse_elf(const uint8_t *image, size_t len, WtaElf *out)
{
    memset(out, 0, sizeof *out);
    if (len < sizeof(WtaEhdr)) {
        snprintf(out->err, sizeof out->err, "file too small (%zu bytes)", len);
        return 0;
    }
    const WtaEhdr *eh = (const WtaEhdr *)image;
    if (memcmp(eh->e_ident, "\x7f" "ELF", 4) != 0) {
        snprintf(out->err, sizeof out->err, "not an ELF");
        return 0;
    }
    if (eh->e_ident[4] != 2 || eh->e_ident[5] != 1) {
        snprintf(out->err, sizeof out->err, "only ELF64 little-endian is supported");
        return 0;
    }
    if (eh->e_type != 2) {
        snprintf(out->err, sizeof out->err, "only static ET_EXEC images are supported (e_type=%u)", eh->e_type);
        return 0;
    }
    if (eh->e_machine != 183) {
        snprintf(out->err, sizeof out->err, "only AArch64 images are supported (e_machine=%u)", eh->e_machine);
        return 0;
    }
    if (eh->e_phnum == 0 || eh->e_phnum > WTA_MAX_PHDR) {
        snprintf(out->err, sizeof out->err, "bad phnum %u", eh->e_phnum);
        return 0;
    }
    if (eh->e_phentsize != sizeof(WtaPhdr)) {
        snprintf(out->err, sizeof out->err, "bad phentsize %u", eh->e_phentsize);
        return 0;
    }
    size_t phbytes = (size_t)eh->e_phnum * sizeof(WtaPhdr);
    if (eh->e_phoff + phbytes > len) {
        snprintf(out->err, sizeof out->err, "program headers out of range");
        return 0;
    }
    out->entry = eh->e_entry;
    out->phoff = eh->e_phoff;
    out->phentsize = eh->e_phentsize;
    out->phnum = eh->e_phnum;

    uint64_t lo = UINT64_MAX, hi = 0;
    uint64_t first_load_off = UINT64_MAX;
    int loads = 0;
    for (int i = 0; i < eh->e_phnum; i++) {
        const WtaPhdr *ph = (const WtaPhdr *)(image + eh->e_phoff + (size_t)i * sizeof(WtaPhdr));
        out->ph[i] = *ph;
        if (ph->p_type == PT_TLS) out->has_tls = 1;
        if (ph->p_type != PT_LOAD) continue;
        loads++;
        if (ph->p_filesz > ph->p_memsz) {
            snprintf(out->err, sizeof out->err, "phdr %d: filesz > memsz", i);
            return 0;
        }
        if (ph->p_vaddr < WTA_PAGE) {
            snprintf(out->err, sizeof out->err, "phdr %d: vaddr below page 1", i);
            return 0;
        }
        if (ph->p_vaddr < lo) lo = ph->p_vaddr;
        if (ph->p_vaddr + ph->p_memsz > hi) hi = ph->p_vaddr + ph->p_memsz;
        if (ph->p_offset < first_load_off) {
            first_load_off = ph->p_offset;
        }
    }
    if (loads == 0) {
        snprintf(out->err, sizeof out->err, "no PT_LOAD segments");
        return 0;
    }
    if (first_load_off != 0) {
        snprintf(out->err, sizeof out->err,
                 "first PT_LOAD does not start at file offset 0 (found 0x%llx)",
                 (unsigned long long)first_load_off);
        return 0;
    }
    /* The phdr table must live inside a mapped PT_LOAD so AT_PHDR resolves. */
    {
        int inside = 0;
        for (int i = 0; i < eh->e_phnum; i++) {
            const WtaPhdr *ph = &out->ph[i];
            if (ph->p_type != PT_LOAD) continue;
            if (eh->e_phoff >= ph->p_offset &&
                eh->e_phoff + phbytes <= ph->p_offset + ph->p_filesz) {
                inside = 1;
                break;
            }
        }
        if (!inside) {
            snprintf(out->err, sizeof out->err, "program headers not inside any PT_LOAD");
            return 0;
        }
    }

    out->map_start = wta_round_down(lo, WTA_PAGE);
    out->map_end = wta_round_up(hi, WTA_PAGE);
    out->ok = 1;
    return 1;
}

/* Map the image at its fixed vaddrs from an already-prepared executable
 * memfd. musl map_library strategy: one RW file mapping over the whole span,
 * zero the bss tails, then mprotect each segment to its final prot — that
 * mprotect is where PROT_EXEC arrives, via the memfd permission class, never
 * as an exec-map of app_data. */
__attribute__((unused))
static int wta_map_image(int memfd, const WtaElf *elf, char *err, size_t errlen)
{
    size_t span = (size_t)(elf->map_end - elf->map_start);
    void *base = mmap((void *)elf->map_start, span, PROT_READ | PROT_WRITE,
                      MAP_PRIVATE | MAP_FIXED, memfd, 0);
    if (base == MAP_FAILED) {
        snprintf(err, errlen, "mmap span %zu at 0x%llx failed: %s",
                 span, (unsigned long long)elf->map_start, strerror(errno));
        return 0;
    }
    for (int i = 0; i < elf->phnum; i++) {
        const WtaPhdr *ph = &elf->ph[i];
        if (ph->p_type != PT_LOAD) continue;
        if (ph->p_memsz > ph->p_filesz) {
            memset((void *)(ph->p_vaddr + ph->p_filesz), 0, ph->p_memsz - ph->p_filesz);
        }
    }
    for (int i = 0; i < elf->phnum; i++) {
        const WtaPhdr *ph = &elf->ph[i];
        if (ph->p_type != PT_LOAD) continue;
        int prot = ((ph->p_flags & 4) ? PROT_EXEC : 0) |
                   ((ph->p_flags & 2) ? PROT_WRITE : 0) |
                   ((ph->p_flags & 1) ? PROT_READ : 0);
        uint64_t s = wta_round_down(ph->p_vaddr, WTA_PAGE);
        uint64_t e = wta_round_up(ph->p_vaddr + ph->p_memsz, WTA_PAGE);
        if (mprotect((void *)s, (size_t)(e - s), prot) != 0) {
            snprintf(err, errlen, "mprotect phdr %d at 0x%llx: %s",
                     i, (unsigned long long)s, strerror(errno));
            return 0;
        }
    }
    for (int i = 0; i < elf->phnum; i++) {
        const WtaPhdr *ph = &elf->ph[i];
        if (ph->p_type != PT_GNU_RELRO || ph->p_memsz == 0) continue;
        uint64_t s = wta_round_down(ph->p_vaddr, WTA_PAGE);
        uint64_t e = wta_round_up(ph->p_vaddr + ph->p_memsz, WTA_PAGE);
        mprotect((void *)s, (size_t)(e - s), PROT_READ);
    }
    return 1;
}

/* ---- initial-stack assembly ------------------------------------------- */
#define WTA_AT_NULL         0
#define WTA_AT_PHDR         3
#define WTA_AT_PHENT        4
#define WTA_AT_PHNUM        5
#define WTA_AT_PAGESZ       6
#define WTA_AT_BASE         7
#define WTA_AT_FLAGS        8
#define WTA_AT_ENTRY        9
#define WTA_AT_UID          11
#define WTA_AT_EUID         12
#define WTA_AT_GID          13
#define WTA_AT_EGID         14
#define WTA_AT_PLATFORM     15
#define WTA_AT_HWCAP        16
#define WTA_AT_CLKTCK       17
#define WTA_AT_SECURE       23
#define WTA_AT_RANDOM       25
#define WTA_AT_EXECFN       31
#define WTA_AT_SYSINFO_EHDR 33

#define WTA_AUX_COUNT 19 /* incl. terminating AT_NULL */

typedef struct {
    uint64_t hwcap;
    uint64_t sysinfo_ehdr;
    char     platform[16];
} WtaParentAux;

typedef struct {
    uint32_t argc;
    uint32_t envc;
    size_t   blob_len;
} WtaStackInfo;

static size_t wta_stack_total(const WtaStackInfo *si)
{
    size_t ptrs = 8UL * (1 + (si->argc + 1) + (si->envc + 1) + 2 * WTA_AUX_COUNT);
    return ptrs + 16 + si->blob_len;
}

#ifndef WTA_CLI_HARNESS

static inline long sys_read(int fd, void *b, size_t n) { return syscall(SYS_read, fd, b, n); }
static inline long sys_write(int fd, const void *b, size_t n) { return syscall(SYS_write, fd, b, n); }
static inline int sys_close(int fd) { return (int)syscall(SYS_close, fd); }
/* aarch64 has no SYS_fork; plain fork(2) is clone(SIGCHLD, ...). */
static inline pid_t sys_fork(void) { return (pid_t)syscall(SYS_clone, SIGCHLD, 0, 0, 0, 0); }

#define WTA_CHILD_ERR_STDIO 1
#define WTA_CHILD_ERR_CHDIR 2
#define WTA_CHILD_ERR_MAP   3
#define WTA_CHILD_ERR_STACK 4

static void wta_child_fail(int sync_wr, int code)
{
    sys_write(sync_wr, &code, 1);
    sys_close(sync_wr);
    syscall(SYS_exit_group, 127);
    for (;;) syscall(SYS_exit, 127);
}

static void wta_child_run(int memfd, const WtaElf *elf,
                          const char *blob, const WtaStackInfo *si,
                          const WtaParentAux *paux,
                          const char *cwd,
                          int fd_in, int fd_out, int fd_err, int sync_wr)
{
    char errbuf[160];

    if (fd_in < 0) {
        fd_in = open("/dev/null", O_RDONLY);
        if (fd_in < 0) wta_child_fail(sync_wr, WTA_CHILD_ERR_STDIO);
    }
    if (dup2(fd_in, 0) < 0 || dup2(fd_out, 1) < 0 || dup2(fd_err, 2) < 0) {
        wta_child_fail(sync_wr, WTA_CHILD_ERR_STDIO);
    }
    if (fd_in > 2) sys_close(fd_in);
    if (fd_out > 2) sys_close(fd_out);
    if (fd_err > 2) sys_close(fd_err);
    if (cwd && cwd[0] && syscall(SYS_chdir, cwd) != 0) {
        wta_child_fail(sync_wr, WTA_CHILD_ERR_CHDIR);
    }

    for (int sig = 1; sig <= 64; sig++) {
        struct sigaction sa;
        memset(&sa, 0, sizeof sa);
        sa.sa_handler = SIG_DFL;
        syscall(SYS_rt_sigaction, sig, &sa, NULL, 8);
    }
    {
        uint64_t empty = 0;
        syscall(SYS_rt_sigprocmask, SIG_SETMASK, &empty, NULL, 8);
    }

    if (!wta_map_image(memfd, elf, errbuf, sizeof errbuf)) {
        size_t n = strlen(errbuf);
        sys_write(2, errbuf, n);
        sys_write(2, "\n", 1);
        wta_child_fail(sync_wr, WTA_CHILD_ERR_MAP);
    }
    sys_close(memfd);

    void *stack = (void *)syscall(SYS_mmap, NULL, WTA_STACK_SIZE,
                                  PROT_READ | PROT_WRITE,
                                  MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
    if ((long)stack == -1) wta_child_fail(sync_wr, WTA_CHILD_ERR_STACK);

    size_t total = wta_stack_total(si);
    char *top = (char *)stack + WTA_STACK_SIZE;
    uint64_t sp = ((uint64_t)(size_t)top - total) & ~0xFUL;
    char *strings = (char *)(sp + total - 16 - si->blob_len);
    memcpy(strings, blob, si->blob_len);

    const char *p = strings;
    for (uint32_t i = 0; i < si->argc; i++) p += strlen(p) + 1;
    for (uint32_t i = 0; i < si->envc; i++) p += strlen(p) + 1;
    const char *platform = p; p += strlen(p) + 1;
    const char *random16 = p; p += 16;
    const char *execfn = p;

    uint64_t *w = (uint64_t *)sp;
    *w++ = si->argc;
    const char *q = strings;
    for (uint32_t i = 0; i < si->argc; i++) { *w++ = (uint64_t)(size_t)q; q += strlen(q) + 1; }
    *w++ = 0;
    for (uint32_t i = 0; i < si->envc; i++) { *w++ = (uint64_t)(size_t)q; q += strlen(q) + 1; }
    *w++ = 0;

    const uint64_t keys[WTA_AUX_COUNT] = {
        WTA_AT_PHDR, WTA_AT_PHENT, WTA_AT_PHNUM, WTA_AT_PAGESZ, WTA_AT_BASE,
        WTA_AT_FLAGS, WTA_AT_ENTRY, WTA_AT_UID, WTA_AT_EUID, WTA_AT_GID,
        WTA_AT_EGID, WTA_AT_PLATFORM, WTA_AT_HWCAP, WTA_AT_CLKTCK, WTA_AT_SECURE,
        WTA_AT_RANDOM, WTA_AT_EXECFN, WTA_AT_SYSINFO_EHDR, WTA_AT_NULL
    };
    const uint64_t vals[WTA_AUX_COUNT] = {
        elf->map_start + elf->phoff,
        elf->phentsize,
        elf->phnum,
        WTA_PAGE,
        0,
        0,
        elf->entry,
        (uint64_t)getuid(),
        (uint64_t)geteuid(),
        (uint64_t)getgid(),
        (uint64_t)getegid(),
        (uint64_t)(size_t)platform,
        paux->hwcap,
        100,
        0,
        (uint64_t)(size_t)random16,
        (uint64_t)(size_t)execfn,
        paux->sysinfo_ehdr,
        0
    };
    for (int i = 0; i < WTA_AUX_COUNT; i++) { *w++ = keys[i]; *w++ = vals[i]; }

    sys_close(sync_wr);

    register uint64_t x_sp asm("x0") = sp;
    register uint64_t x_entry asm("x1") = elf->entry;
    asm volatile(
        "mov sp, x0\n"
        "mov x0, xzr\n"
        "mov x1, xzr\n"
        "mov x2, xzr\n"
        "mov x3, xzr\n"
        "mov x4, xzr\n"
        "mov x5, xzr\n"
        "mov x6, xzr\n"
        "mov x7, xzr\n"
        "mov x8, xzr\n"
        "mov x9, xzr\n"
        "mov x10, xzr\n"
        "mov x11, xzr\n"
        "mov x12, xzr\n"
        "mov x13, xzr\n"
        "mov x14, xzr\n"
        "mov x15, xzr\n"
        "mov x17, xzr\n"
        "mov x18, xzr\n"
        "br x1\n"
        :: "r"(x_sp), "r"(x_entry) : "memory");
    __builtin_unreachable();
}

/* ---- parent-side preparation ------------------------------------------ */

static int wta_copy_to_memfd(int src_fd, off_t resume_at, const uint8_t *head, size_t head_len, int *out)
{
    int m;
#ifdef SYS_memfd_create
    /* MFD_CLOEXEC(2) | MFD_EXEC(0x10); older kernels reject MFD_EXEC(EINVAL). */
    m = (int)syscall(SYS_memfd_create, "wta-static-exec", 2 | 0x10);
    if (m < 0 && errno == EINVAL) m = (int)syscall(SYS_memfd_create, "wta-static-exec", 2);
#else
    m = -1; errno = ENOSYS;
#endif
    if (m < 0) return 0;
    if (lseek(m, 0, SEEK_SET) != 0) { close(m); return 0; }
    if (resume_at > 0 && lseek(src_fd, resume_at, SEEK_SET) == (off_t)-1) { close(m); return 0; }
    if (head_len && write(m, head, head_len) != (ssize_t)head_len) { close(m); return 0; }
    char buf[65536];
    ssize_t r;
    while ((r = read(src_fd, buf, sizeof buf)) > 0) {
        ssize_t off = 0;
        while (off < r) {
            ssize_t w2 = write(m, buf + off, (size_t)(r - off));
            if (w2 < 0) { close(m); return 0; }
            off += w2;
        }
    }
    if (r < 0) { close(m); return 0; }
    *out = m;
    return 1;
}

static void wta_read_parent_aux(WtaParentAux *paux)
{
    memset(paux, 0, sizeof *paux);
    strcpy(paux->platform, "aarch64");
    int fd = open("/proc/self/auxv", O_RDONLY | O_CLOEXEC);
    if (fd < 0) return;
    uint64_t kv[2];
    while (read(fd, kv, sizeof kv) == (ssize_t)sizeof kv) {
        if (kv[0] == WTA_AT_HWCAP) paux->hwcap = kv[1];
        else if (kv[0] == WTA_AT_SYSINFO_EHDR) paux->sysinfo_ehdr = kv[1];
        else if (kv[0] == WTA_AT_PLATFORM && kv[1]) {
            const char *s = (const char *)(size_t)kv[1];
            size_t i = 0;
            while (s[i] && i < sizeof paux->platform - 1) { paux->platform[i] = s[i]; i++; }
            paux->platform[i] = 0;
        } else if (kv[0] == WTA_AT_NULL) break;
    }
    close(fd);
}

/* Serialize [argv strings][envp strings][platform][16B random][execfn]. */
static char *wta_build_blob(JNIEnv *env, jobjectArray jargv, jobjectArray jenvp,
                            uint32_t *argc_out, uint32_t *envc_out, size_t *blob_len_out,
                            const WtaParentAux *paux)
{
    uint32_t argc = (uint32_t)(*env)->GetArrayLength(env, jargv);
    uint32_t envc = (uint32_t)(*env)->GetArrayLength(env, jenvp);

    if ((*env)->PushLocalFrame(env, (jint)(argc + envc + 16)) != 0) return NULL;

    char **avs = calloc(argc + 1, sizeof(char *));
    char **evs = calloc(envc + 1, sizeof(char *));
    if (!avs || !evs) {
        free(avs); free(evs);
        (*env)->PopLocalFrame(env, NULL);
        return NULL;
    }
    size_t need = 1;
    for (uint32_t i = 0; i < argc; i++) {
        jstring s = (jstring)(*env)->GetObjectArrayElement(env, jargv, i);
        avs[i] = (char *)(*env)->GetStringUTFChars(env, s, NULL);
        need += strlen(avs[i]) + 1;
    }
    for (uint32_t i = 0; i < envc; i++) {
        jstring s = (jstring)(*env)->GetObjectArrayElement(env, jenvp, i);
        evs[i] = (char *)(*env)->GetStringUTFChars(env, s, NULL);
        need += strlen(evs[i]) + 1;
    }
    need += strlen(paux->platform) + 1 + 16 + strlen(avs[0]) + 1;

    char *blob = malloc(need);
    char *w = blob;
    if (blob) {
        for (uint32_t i = 0; i < argc; i++) { size_t l = strlen(avs[i]) + 1; memcpy(w, avs[i], l); w += l; }
        for (uint32_t i = 0; i < envc; i++) { size_t l = strlen(evs[i]) + 1; memcpy(w, evs[i], l); w += l; }
        { size_t l = strlen(paux->platform) + 1; memcpy(w, paux->platform, l); w += l; }
        {
            int ur = open("/dev/urandom", O_RDONLY | O_CLOEXEC);
            if (ur >= 0) {
                if (read(ur, w, 16) != 16) memset(w, 0x42, 16);
                close(ur);
            } else {
                memset(w, 0x42, 16);
            }
            w += 16;
        }
        { size_t l = strlen(avs[0]) + 1; memcpy(w, avs[0], l); w += l; }
    }

    for (uint32_t i = 0; i < argc; i++) {
        jstring s = (jstring)(*env)->GetObjectArrayElement(env, jargv, i);
        (*env)->ReleaseStringUTFChars(env, s, avs[i]);
    }
    for (uint32_t i = 0; i < envc; i++) {
        jstring s = (jstring)(*env)->GetObjectArrayElement(env, jenvp, i);
        (*env)->ReleaseStringUTFChars(env, s, evs[i]);
    }
    free(avs); free(evs);
    (*env)->PopLocalFrame(env, NULL);

    if (!blob) return NULL;
    *argc_out = argc;
    *envc_out = envc;
    *blob_len_out = (size_t)(w - blob);
    return blob;
}

JNIEXPORT jlong JNICALL
Java_com_webtoapp_core_linux_StaticExecBridge_nativeSpawn(
        JNIEnv *env, jclass clazz,
        jstring jpath, jobjectArray jargv, jobjectArray jenvp, jstring jcwd,
        jint fd_in, jint fd_out, jint fd_err, jintArray jerr)
{
    (void)clazz;
    jint err_code = 0, err_detail = 0;
    pid_t pid = -1;
    int src = -1, memfd = -1;
    char *blob = NULL;
    int sync_pipe[2] = { -1, -1 };
    const char *path = (*env)->GetStringUTFChars(env, jpath, NULL);
    const char *cwd = jcwd ? (*env)->GetStringUTFChars(env, jcwd, NULL) : NULL;

    do {
        src = open(path, O_RDONLY | O_CLOEXEC);
        if (src < 0) { err_code = 1; err_detail = errno; break; }

        uint8_t head[sizeof(WtaEhdr) + WTA_MAX_PHDR * sizeof(WtaPhdr)];
        ssize_t got = read(src, head, sizeof head);
        if (got < (ssize_t)sizeof(WtaEhdr)) { err_code = 2; err_detail = errno; break; }

        WtaElf elf;
        if (!wta_parse_elf(head, (size_t)got, &elf)) { err_code = 3; err_detail = 0; break; }

        if (!wta_copy_to_memfd(src, (off_t)got, head, (size_t)got, &memfd)) {
            err_code = 5; err_detail = errno; break;
        }
        close(src); src = -1;

        WtaParentAux paux;
        wta_read_parent_aux(&paux);

        WtaStackInfo si;
        blob = wta_build_blob(env, jargv, jenvp, &si.argc, &si.envc, &si.blob_len, &paux);
        if (!blob) { err_code = 6; err_detail = errno; break; }

        if (pipe(sync_pipe) != 0) { err_code = 7; err_detail = errno; break; }

        pid = sys_fork();
        if (pid < 0) { err_code = 8; err_detail = errno; break; }
        if (pid == 0) {
            close(sync_pipe[0]);
            wta_child_run(memfd, &elf, blob, &si, &paux, cwd, fd_in, fd_out, fd_err, sync_pipe[1]);
            __builtin_unreachable();
        }

        close(sync_pipe[1]); sync_pipe[1] = -1;
        uint8_t child_err = 0;
        ssize_t n = read(sync_pipe[0], &child_err, 1);
        close(sync_pipe[0]); sync_pipe[0] = -1;
        if (n == 1) {
            int status;
            waitpid(pid, &status, 0);
            err_code = 100 + (int)child_err;
            pid = -1;
        }
    } while (0);

    if (sync_pipe[0] >= 0) close(sync_pipe[0]);
    if (sync_pipe[1] >= 0) close(sync_pipe[1]);
    if (src >= 0) close(src);
    if (memfd >= 0) close(memfd);
    free(blob);
    (*env)->ReleaseStringUTFChars(env, jpath, path);
    if (cwd) (*env)->ReleaseStringUTFChars(env, jcwd, cwd);
    if (jerr && err_code != 0) {
        jint buf[2] = { err_code, err_detail };
        (*env)->SetIntArrayRegion(env, jerr, 0, 2, buf);
    }
    return (jlong)pid;
}

JNIEXPORT jint JNICALL
Java_com_webtoapp_core_linux_StaticExecBridge_nativeWaitpid(
        JNIEnv *env, jclass clazz, jlong pid, jintArray statusOut, jboolean blocking)
{
    (void)clazz;
    int status = 0;
    pid_t r = waitpid((pid_t)pid, &status, blocking ? 0 : WNOHANG);
    if (r == 0) return 0; /* still running (non-blocking poll) */
    if (r < 0) return -1;
    if (statusOut) {
        jint s = (jint)status;
        (*env)->SetIntArrayRegion(env, statusOut, 0, 1, &s);
    }
    return 1; /* reaped */
}

JNIEXPORT jint JNICALL
Java_com_webtoapp_core_linux_StaticExecBridge_nativeKill(
        JNIEnv *env, jclass clazz, jlong pid, jint sig)
{
    (void)env; (void)clazz;
    return kill((pid_t)pid, sig);
}

#else /* WTA_CLI_HARNESS */

int main(int argc, char **argv)
{
    if (argc < 2) { fprintf(stderr, "usage: %s <static-elf>\n", argv[0]); return 2; }
    int fd = open(argv[1], O_RDONLY);
    if (fd < 0) { perror("open"); return 2; }
    static uint8_t img[64 << 20];
    ssize_t total = 0;
    while (1) {
        ssize_t r = read(fd, img + total, sizeof img - (size_t)total);
        if (r < 0) { perror("read"); return 2; }
        if (r == 0) break;
        total += r;
    }
    close(fd);

    WtaElf elf;
    if (!wta_parse_elf(img, (size_t)total, &elf)) {
        fprintf(stderr, "parse failed: %s\n", elf.err);
        return 1;
    }
    printf("entry      : 0x%llx\n", (unsigned long long)elf.entry);
    printf("phoff      : 0x%llx phnum %u phentsize %u\n",
           (unsigned long long)elf.phoff, elf.phnum, elf.phentsize);
    printf("map span   : [0x%llx, 0x%llx) (%zu pages)\n",
           (unsigned long long)elf.map_start, (unsigned long long)elf.map_end,
           (size_t)((elf.map_end - elf.map_start) / WTA_PAGE));
    printf("at_phdr    : 0x%llx\n", (unsigned long long)(elf.map_start + elf.phoff));
    printf("tls        : %s\n", elf.has_tls ? "PT_TLS present" : "none");
    for (int i = 0; i < elf.phnum; i++) {
        const WtaPhdr *ph = &elf.ph[i];
        printf("phdr[%d]    : type=%#x flags=%u off=0x%llx vaddr=0x%llx filesz=0x%llx memsz=0x%llx\n",
               i, ph->p_type, ph->p_flags,
               (unsigned long long)ph->p_offset, (unsigned long long)ph->p_vaddr,
               (unsigned long long)ph->p_filesz, (unsigned long long)ph->p_memsz);
    }
    WtaStackInfo si = { .argc = 2, .envc = 2, .blob_len = 128 };
    printf("stack      : %zu bytes (argc=%u envc=%u)\n", wta_stack_total(&si), si.argc, si.envc);
    printf("OK\n");
    return 0;
}

#endif /* WTA_CLI_HARNESS */
