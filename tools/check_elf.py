#!/usr/bin/env python3
"""Inspect PT_LOAD segments of an ELF64 LSB file (mirrors NodeDependencyManager.kt logic).

Used to verify that nodejs-mobile's libnode.so has 16KB-aligned vaddr/offset,
which is the prerequisite for in-place p_align patching.
"""
import struct
import sys

def main(path):
    with open(path, "rb") as f:
        data = f.read()
    if data[:4] != b"\x7fELF":
        print("not an ELF")
        return
    elf_class = data[4]
    elf_data = data[5]
    if elf_class != 2 or elf_data != 1:
        print(f"unsupported ELF: class={elf_class} data={elf_data}")
        return

    e_phoff = struct.unpack_from("<Q", data, 32)[0]
    e_phentsize = struct.unpack_from("<H", data, 54)[0]
    e_phnum = struct.unpack_from("<H", data, 56)[0]
    print(f"e_phoff=0x{e_phoff:x} e_phentsize={e_phentsize} e_phnum={e_phnum}")

    target = 16 * 1024
    mask = target - 1
    pt_loads = []
    bad = []
    for i in range(e_phnum):
        base = e_phoff + i * e_phentsize
        p_type, p_flags = struct.unpack_from("<II", data, base)
        if p_type != 1:
            continue
        p_offset = struct.unpack_from("<Q", data, base + 8)[0]
        p_vaddr  = struct.unpack_from("<Q", data, base + 16)[0]
        p_filesz = struct.unpack_from("<Q", data, base + 32)[0]
        p_memsz  = struct.unpack_from("<Q", data, base + 40)[0]
        p_align  = struct.unpack_from("<Q", data, base + 48)[0]
        # The bionic linker requires:
        #   p_align >= page_size       (otherwise dlopen rejects upfront)
        #   (p_vaddr - p_offset) mod page_size == 0
        # The latter is the actual mmap invariant: the kernel rounds
        # p_vaddr down to page_size to compute the mmap base, and the
        # corresponding file offset must round the same way to keep the
        # bytes lined up with virtual addresses.
        align_ok = p_align >= target
        consistent = ((p_vaddr - p_offset) & mask) == 0
        pt_loads.append({
            "i": i,
            "p_offset": p_offset,
            "p_vaddr": p_vaddr,
            "p_filesz": p_filesz,
            "p_memsz": p_memsz,
            "p_align": p_align,
            "align_ok": align_ok,
            "consistent": consistent,
        })
        if not align_ok or not consistent:
            bad.append(i)

    print(f"Found {len(pt_loads)} PT_LOAD segments:")
    for s in pt_loads:
        flags = "OK"
        if not s["align_ok"]:
            flags += " ALIGN_TOO_SMALL"
        if not s["consistent"]:
            flags += " VADDR_OFFSET_DELTA_NOT_PAGE_ALIGNED"
        print(f"  #{s['i']}: vaddr=0x{s['p_vaddr']:x} offset=0x{s['p_offset']:x} "
              f"filesz=0x{s['p_filesz']:x} memsz=0x{s['p_memsz']:x} "
              f"align=0x{s['p_align']:x}  -> {flags}")

    if bad:
        print(f"\n[NEEDS-PATCH] PT_LOAD #{bad} are incompatible with 16KB page kernel")
    else:
        print("\n[OK] all PT_LOAD segments are loadable on a 16KB page kernel")

if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else "/tmp/wta-test/libnode.so")
