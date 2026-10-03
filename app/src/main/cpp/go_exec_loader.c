#include <errno.h>
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/syscall.h>
#include <unistd.h>

#ifndef EI_CLASS
#define EI_CLASS 4
#endif

#ifndef ELFCLASS32
#define ELFCLASS32 1
#endif

#ifndef ELFCLASS64
#define ELFCLASS64 2
#endif

#define ET_EXEC 2
#define ET_DYN  3

#ifndef AT_EMPTY_PATH
#define AT_EMPTY_PATH 0x1000
#endif

#ifndef MFD_CLOEXEC
#define MFD_CLOEXEC 0x0001U
#endif

#ifndef MFD_ALLOW_SEALING
#define MFD_ALLOW_SEALING 0x0002U
#endif

#ifndef MFD_EXEC
#define MFD_EXEC 0x0010U
#endif
#ifndef MFD_NOEXEC_SEAL
#define MFD_NOEXEC_SEAL 0x0008U
#endif

#ifndef F_LINUX_SPECIFIC_BASE
#define F_LINUX_SPECIFIC_BASE 1024
#endif
#ifndef F_ADD_SEALS
#define F_ADD_SEALS  (F_LINUX_SPECIFIC_BASE + 9)
#endif
#ifndef F_SEAL_SEAL
#define F_SEAL_SEAL    0x0001
#endif
#ifndef F_SEAL_SHRINK
#define F_SEAL_SHRINK  0x0002
#endif
#ifndef F_SEAL_GROW
#define F_SEAL_GROW    0x0004
#endif
#ifndef F_SEAL_WRITE
#define F_SEAL_WRITE   0x0008
#endif

#define LINKER32_PATH "/system/bin/linker"
#define LINKER64_PATH "/system/bin/linker64"

extern char** environ;

static int create_memfd(const char* name) {

    unsigned int flags = MFD_CLOEXEC | MFD_ALLOW_SEALING | MFD_EXEC;
    int fd;
#ifdef SYS_memfd_create
    fd = (int) syscall(SYS_memfd_create, name, flags);
#elif defined(__NR_memfd_create)
    fd = (int) syscall(__NR_memfd_create, name, flags);
#else
    errno = ENOSYS;
    return -1;
#endif
    if (fd >= 0) return fd;
    if (errno != EINVAL) return -1;

    flags = MFD_CLOEXEC | MFD_ALLOW_SEALING;
#ifdef SYS_memfd_create
    return (int) syscall(SYS_memfd_create, name, flags);
#elif defined(__NR_memfd_create)
    return (int) syscall(__NR_memfd_create, name, flags);
#else
    errno = ENOSYS;
    return -1;
#endif
}

static int execveat_empty_path(int fd, char* const argv[]) {
#ifdef SYS_execveat
    return (int) syscall(SYS_execveat, fd, "", argv, environ, AT_EMPTY_PATH);
#elif defined(__NR_execveat)
    return (int) syscall(__NR_execveat, fd, "", argv, environ, AT_EMPTY_PATH);
#else
    errno = ENOSYS;
    return -1;
#endif
}

static int copy_fd(int in_fd, int out_fd) {
    char buffer[64 * 1024];
    while (1) {
        ssize_t read_count = read(in_fd, buffer, sizeof(buffer));
        if (read_count == 0) return 0;
        if (read_count < 0) {
            if (errno == EINTR) continue;
            return -1;
        }

        ssize_t written = 0;
        while (written < read_count) {
            ssize_t write_count = write(out_fd, buffer + written, (size_t) (read_count - written));
            if (write_count < 0) {
                if (errno == EINTR) continue;
                return -1;
            }
            written += write_count;
        }
    }
}

static const char* choose_linker_path(int source_fd) {

    unsigned char header[20];
    if (lseek(source_fd, 0, SEEK_SET) < 0) {
        return NULL;
    }

    ssize_t read_count = read(source_fd, header, sizeof(header));
    if (read_count < (ssize_t) sizeof(header)) {
        return NULL;
    }

    if (header[0] != 0x7f || header[1] != 'E' || header[2] != 'L' || header[3] != 'F') {
        return NULL;
    }

    unsigned int e_type = (unsigned int) header[16] | ((unsigned int) header[17] << 8);
    if (e_type != ET_DYN) {

        return NULL;
    }

    if (header[EI_CLASS] == ELFCLASS64) {
        return LINKER64_PATH;
    }
    if (header[EI_CLASS] == ELFCLASS32) {
        return LINKER32_PATH;
    }
    return NULL;
}

int main(int argc, char* argv[]) {
    if (argc < 2) {
        fprintf(stderr, "Usage: %s <executable> [args...]\n", argv[0]);
        return 64;
    }

    char* target_path = argv[1];

    execv(target_path, &argv[1]);
    if (errno != EACCES && errno != EPERM) {
        fprintf(stderr, "execv failed for %s: %s\n", target_path, strerror(errno));
        return 111;
    }

    int source_fd = open(target_path, O_RDONLY | O_CLOEXEC);
    if (source_fd < 0) {
        fprintf(stderr, "open failed for %s: %s\n", target_path, strerror(errno));
        return 112;
    }

    const char* linker_path = choose_linker_path(source_fd);
    if (linker_path != NULL) {
        execve(linker_path, argv, environ);
        fprintf(stderr, "linker exec failed for %s via %s: %s\n",
                target_path, linker_path, strerror(errno));
    }

    if (lseek(source_fd, 0, SEEK_SET) < 0) {
        fprintf(stderr, "lseek failed for %s: %s\n", target_path, strerror(errno));
        close(source_fd);
        return 117;
    }

    int memfd = create_memfd("wta-go-exec");
    if (memfd < 0) {
        fprintf(stderr, "memfd_create failed: %s\n", strerror(errno));
        close(source_fd);
        return 113;
    }

    if (copy_fd(source_fd, memfd) != 0) {
        fprintf(stderr, "copy to memfd failed for %s: %s\n", target_path, strerror(errno));
        close(source_fd);
        close(memfd);
        return 114;
    }

    close(source_fd);

    if (fcntl(memfd, F_ADD_SEALS,
              F_SEAL_SEAL | F_SEAL_SHRINK | F_SEAL_GROW | F_SEAL_WRITE) < 0) {
        fprintf(stderr, "fcntl(F_ADD_SEALS) failed for %s: %s (continuing)\n",
                target_path, strerror(errno));
    }

    if (execveat_empty_path(memfd, &argv[1]) != 0) {
        fprintf(stderr, "execveat failed for %s: %s\n", target_path, strerror(errno));
        close(memfd);
        return 115;
    }

    return 116;
}
