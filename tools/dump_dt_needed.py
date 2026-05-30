#!/usr/bin/env python3
"""Dump DT_NEEDED entries from an ELF64 LSB shared library."""
import struct
import sys

DT_NEEDED = 1
DT_STRTAB = 5

def main(path):
    with open(path, "rb") as f:
        data = f.read()
    e_phoff = struct.unpack_from("<Q", data, 32)[0]
    e_phentsize = struct.unpack_from("<H", data, 54)[0]
    e_phnum = struct.unpack_from("<H", data, 56)[0]
    dyn_off = None
    dyn_size = None
    pt_loads = []
    for i in range(e_phnum):
        base = e_phoff + i * e_phentsize
        p_type = struct.unpack_from("<I", data, base)[0]
        p_offset = struct.unpack_from("<Q", data, base + 8)[0]
        p_vaddr  = struct.unpack_from("<Q", data, base + 16)[0]
        p_filesz = struct.unpack_from("<Q", data, base + 32)[0]
        p_align  = struct.unpack_from("<Q", data, base + 48)[0]
        if p_type == 1:
            pt_loads.append((p_offset, p_vaddr, p_filesz, p_align))
        if p_type == 2:  # PT_DYNAMIC
            dyn_off = p_offset
            dyn_size = p_filesz

    print("PT_LOAD segments:")
    for o, v, sz, a in pt_loads:
        print(f"  offset=0x{o:x} vaddr=0x{v:x} filesz=0x{sz:x} align=0x{a:x}")

    if dyn_off is None:
        print("(no PT_DYNAMIC)")
        return

    # Walk dyn entries to find DT_STRTAB and DT_NEEDED list
    needed_indices = []
    strtab_vaddr = None
    n = dyn_size // 16
    for i in range(n):
        d_tag = struct.unpack_from("<q", data, dyn_off + i * 16)[0]
        d_val = struct.unpack_from("<Q", data, dyn_off + i * 16 + 8)[0]
        if d_tag == DT_NEEDED:
            needed_indices.append(d_val)
        elif d_tag == DT_STRTAB:
            strtab_vaddr = d_val
        elif d_tag == 0:
            break

    if strtab_vaddr is None:
        print("(no DT_STRTAB)")
        return

    # Find file offset of strtab via PT_LOADs
    strtab_off = None
    for p_off, p_vaddr, p_fsz, _ in pt_loads:
        if p_vaddr <= strtab_vaddr < p_vaddr + p_fsz:
            strtab_off = p_off + (strtab_vaddr - p_vaddr)
            break
    if strtab_off is None:
        print(f"(strtab vaddr 0x{strtab_vaddr:x} not in any PT_LOAD)")
        return

    print(f"\nstrtab vaddr=0x{strtab_vaddr:x} -> file offset 0x{strtab_off:x}")
    print("DT_NEEDED entries:")
    for idx in needed_indices:
        end = data.index(b"\x00", strtab_off + idx)
        name = data[strtab_off + idx:end].decode("utf-8", "replace")
        print(f"  [{idx}] {name}")

if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else "/tmp/wta-test/libnode.from-device.so")
