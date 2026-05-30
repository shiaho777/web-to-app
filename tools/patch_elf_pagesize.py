#!/usr/bin/env python3
"""Repack an ELF64 LSB shared library to be loadable on a kernel with the
given page size, by inflating each PT_LOAD segment's file offset (and
shifting later segments + section header table) so that
    p_offset mod page_size == p_vaddr mod page_size
for every PT_LOAD. Vaddr is not changed, so all relocations / code stay
correct.

Algorithm:
  1. Parse e_phoff, e_shoff, e_phnum, e_shnum etc. from ELF header.
  2. Parse all program headers; collect PT_LOAD entries in vaddr order.
  3. Compute new file offsets so each PT_LOAD's p_offset & (page-1) ==
     p_vaddr & (page-1), and successive PT_LOADs don't overlap. Pad with
     zeroes between segments as needed.
  4. Build the new file: header at start, program header table (in
     place), then segments at their new offsets, then section header
     table at a fresh offset after the last segment, then non-LOAD
     contents that were referenced by section headers (e.g. .strtab,
     .symtab) re-laid-out.
  5. Update e_shoff, p_offset, p_align (set to page_size), and every
     section header's sh_offset.

The output lib is layout-changed but functionally identical: dlopen
mmaps PT_LOAD segments at their original p_vaddr, and each segment's
file content sits at a 16K-compatible offset inside the file.

Usage:
  patch_elf_pagesize.py <input.so> <output.so> [page_size_bytes]
"""
import struct
import sys
import os

PT_LOAD = 1


def u16(d, o): return struct.unpack_from("<H", d, o)[0]
def u32(d, o): return struct.unpack_from("<I", d, o)[0]
def u64(d, o): return struct.unpack_from("<Q", d, o)[0]


def patch(in_path, out_path, page_size):
    with open(in_path, "rb") as f:
        data = bytearray(f.read())

    if data[:4] != b"\x7fELF":
        raise SystemExit("not an ELF")
    if data[4] != 2 or data[5] != 1:
        raise SystemExit("only ELF64 LSB supported")

    # ELF64 ehdr fields we care about
    e_phoff = u64(data, 32)
    e_shoff = u64(data, 40)
    e_phentsize = u16(data, 54)
    e_phnum = u16(data, 56)
    e_shentsize = u16(data, 58)
    e_shnum = u16(data, 60)

    # Read all program headers
    phdrs = []
    for i in range(e_phnum):
        base = e_phoff + i * e_phentsize
        phdrs.append({
            "i": i,
            "off": base,
            "p_type": u32(data, base + 0),
            "p_flags": u32(data, base + 4),
            "p_offset": u64(data, base + 8),
            "p_vaddr": u64(data, base + 16),
            "p_paddr": u64(data, base + 24),
            "p_filesz": u64(data, base + 32),
            "p_memsz": u64(data, base + 40),
            "p_align": u64(data, base + 48),
        })

    # Read all section headers (we need to remap their sh_offset later)
    shdrs = []
    for i in range(e_shnum):
        base = e_shoff + i * e_shentsize
        shdrs.append({
            "i": i,
            "off": base,
            "sh_name": u32(data, base + 0),
            "sh_type": u32(data, base + 4),
            "sh_flags": u64(data, base + 8),
            "sh_addr": u64(data, base + 16),
            "sh_offset": u64(data, base + 24),
            "sh_size": u64(data, base + 32),
            "sh_link": u32(data, base + 40),
            "sh_info": u32(data, base + 44),
            "sh_addralign": u64(data, base + 48),
            "sh_entsize": u64(data, base + 56),
        })

    # Collect PT_LOADs in vaddr order. Each gets a new file offset.
    loads = sorted([p for p in phdrs if p["p_type"] == PT_LOAD], key=lambda p: p["p_vaddr"])

    new_offsets = {}  # phdr index -> new p_offset
    cur = max((p["p_offset"] + p["p_filesz"]) for p in phdrs if p["p_type"] != PT_LOAD)
    # Start placement after the program header table. First segment's
    # original offset is typically 0 (it covers the ELF header itself);
    # we don't move that one — it's already 16K-compatible because vaddr=0.
    # For subsequent segments, place at the smallest offset >= prev_end such
    # that (offset mod page) == (vaddr mod page).
    mask = page_size - 1
    prev_end = 0
    for s in loads:
        if s["p_offset"] == 0 and s["p_vaddr"] == 0:
            new_offsets[s["i"]] = 0
            prev_end = max(prev_end, s["p_filesz"])
            continue
        # find new offset such that:
        #   new_off >= prev_end
        #   new_off mod page == vaddr mod page
        target_mod = s["p_vaddr"] & mask
        rounded = ((prev_end + mask) & ~mask)  # align up to page
        new_off = rounded + target_mod
        if new_off < prev_end:
            new_off += page_size
        new_offsets[s["i"]] = new_off
        prev_end = new_off + s["p_filesz"]

    # Compute new section header table offset: place after the last load segment.
    new_shoff = ((prev_end + mask) & ~mask)

    # Compute final file size
    # Section header table size + section data that lives outside any PT_LOAD
    # (e.g., debug sections, .symtab, .strtab, .shstrtab). We need to copy
    # these too.
    non_load_sections = []
    for sh in shdrs:
        if sh["sh_type"] == 0:  # SHT_NULL
            continue
        if sh["sh_size"] == 0:
            continue
        if sh["sh_flags"] & 0x2:  # SHF_ALLOC
            # this section is part of a PT_LOAD; do not relocate
            continue
        non_load_sections.append(sh)

    # Place non-load section data after the section header table.
    sht_size = e_shentsize * e_shnum
    cursor = new_shoff + sht_size
    new_sh_offsets = {}
    for sh in non_load_sections:
        # Align to sh_addralign for the section
        align = max(1, sh["sh_addralign"])
        cursor = ((cursor + align - 1) // align) * align
        new_sh_offsets[sh["i"]] = cursor
        cursor += sh["sh_size"]

    final_size = cursor

    # Build new file
    out = bytearray(final_size)

    # 1. Copy ELF header (first 64 bytes)
    out[:64] = data[:64]

    # 2. Copy program header table at e_phoff (kept in place)
    out[e_phoff:e_phoff + e_phnum * e_phentsize] = data[e_phoff:e_phoff + e_phnum * e_phentsize]

    # 3. Copy each PT_LOAD segment to its new location, then update its phdr
    for s in loads:
        new_off = new_offsets[s["i"]]
        old_off = s["p_offset"]
        size = s["p_filesz"]
        if size > 0:
            out[new_off:new_off + size] = data[old_off:old_off + size]
        # Update p_offset and p_align in the program header
        ph_base = s["off"]
        struct.pack_into("<Q", out, ph_base + 8, new_off)
        struct.pack_into("<Q", out, ph_base + 48, page_size)

    # 4. Update non-PT_LOAD program headers (e.g., PT_DYNAMIC, PT_INTERP,
    #    PT_GNU_RELRO, PT_NOTE) — their p_offset usually points inside a
    #    PT_LOAD segment, so we shift it by the same amount.
    for p in phdrs:
        if p["p_type"] == PT_LOAD:
            continue
        # find which PT_LOAD segment contains this offset
        for s in loads:
            old_start = s["p_offset"]
            old_end = old_start + s["p_filesz"]
            if old_start <= p["p_offset"] < old_end:
                delta = new_offsets[s["i"]] - old_start
                ph_base = p["off"]
                struct.pack_into("<Q", out, ph_base + 8, p["p_offset"] + delta)
                break
        else:
            # offset not inside any PT_LOAD — could be in shstrtab or similar.
            # Best-effort: leave as-is. dlopen rarely uses these.
            pass

    # 5. Place section header table at new_shoff with updated sh_offsets
    for sh in shdrs:
        new_off = sh["sh_offset"]
        if sh["sh_type"] == 0:
            new_off = 0
        elif sh["sh_flags"] & 0x2:  # SHF_ALLOC: inside a PT_LOAD
            for s in loads:
                old_start = s["p_offset"]
                old_end = old_start + s["p_filesz"]
                if old_start <= sh["sh_offset"] < old_end:
                    new_off = sh["sh_offset"] + (new_offsets[s["i"]] - old_start)
                    break
        elif sh["i"] in new_sh_offsets:
            new_off = new_sh_offsets[sh["i"]]

        # Write the updated section header into the output's SHT
        sh_dst = new_shoff + sh["i"] * e_shentsize
        struct.pack_into("<I", out, sh_dst + 0, sh["sh_name"])
        struct.pack_into("<I", out, sh_dst + 4, sh["sh_type"])
        struct.pack_into("<Q", out, sh_dst + 8, sh["sh_flags"])
        struct.pack_into("<Q", out, sh_dst + 16, sh["sh_addr"])
        struct.pack_into("<Q", out, sh_dst + 24, new_off)
        struct.pack_into("<Q", out, sh_dst + 32, sh["sh_size"])
        struct.pack_into("<I", out, sh_dst + 40, sh["sh_link"])
        struct.pack_into("<I", out, sh_dst + 44, sh["sh_info"])
        struct.pack_into("<Q", out, sh_dst + 48, sh["sh_addralign"])
        struct.pack_into("<Q", out, sh_dst + 56, sh["sh_entsize"])

    # 6. Copy non-load sections to their new homes
    for sh in non_load_sections:
        new_off = new_sh_offsets[sh["i"]]
        old_off = sh["sh_offset"]
        size = sh["sh_size"]
        if size > 0:
            out[new_off:new_off + size] = data[old_off:old_off + size]

    # 7. Update e_shoff
    struct.pack_into("<Q", out, 40, new_shoff)

    with open(out_path, "wb") as f:
        f.write(out)

    print(f"Patched: {in_path} ({len(data)} bytes) -> {out_path} ({len(out)} bytes)")
    print(f"page_size={page_size}, new_shoff=0x{new_shoff:x}")
    for s in loads:
        print(f"  PT_LOAD #{s['i']}: vaddr=0x{s['p_vaddr']:x} "
              f"old_offset=0x{s['p_offset']:x} -> new_offset=0x{new_offsets[s['i']]:x} "
              f"(filesz=0x{s['p_filesz']:x})")


if __name__ == "__main__":
    if len(sys.argv) < 3:
        print(__doc__)
        sys.exit(1)
    in_path = sys.argv[1]
    out_path = sys.argv[2]
    page_size = int(sys.argv[3]) if len(sys.argv) > 3 else 16384
    patch(in_path, out_path, page_size)
