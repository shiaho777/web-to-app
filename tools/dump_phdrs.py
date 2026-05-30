#!/usr/bin/env python3
"""Dump all program headers (not just PT_LOAD)."""
import struct
import sys

PT_NULL=0; PT_LOAD=1; PT_DYNAMIC=2; PT_INTERP=3; PT_NOTE=4
PT_SHLIB=5; PT_PHDR=6; PT_TLS=7
PT_GNU_EH_FRAME=0x6474e550
PT_GNU_STACK=0x6474e551
PT_GNU_RELRO=0x6474e552
PT_GNU_PROPERTY=0x6474e553

NAMES = {
    PT_NULL:"PT_NULL", PT_LOAD:"PT_LOAD", PT_DYNAMIC:"PT_DYNAMIC",
    PT_INTERP:"PT_INTERP", PT_NOTE:"PT_NOTE", PT_SHLIB:"PT_SHLIB",
    PT_PHDR:"PT_PHDR", PT_TLS:"PT_TLS",
    PT_GNU_EH_FRAME:"PT_GNU_EH_FRAME", PT_GNU_STACK:"PT_GNU_STACK",
    PT_GNU_RELRO:"PT_GNU_RELRO", PT_GNU_PROPERTY:"PT_GNU_PROPERTY",
}

def main(path):
    with open(path, "rb") as f:
        data = f.read()
    e_phoff = struct.unpack_from("<Q", data, 32)[0]
    e_phentsize = struct.unpack_from("<H", data, 54)[0]
    e_phnum = struct.unpack_from("<H", data, 56)[0]
    print(f"e_phoff=0x{e_phoff:x} e_phentsize={e_phentsize} e_phnum={e_phnum}")
    print(f"{'idx':>3}  {'type':<20} {'offset':>14} {'vaddr':>14} {'filesz':>14} {'memsz':>14} {'align':>8}")
    for i in range(e_phnum):
        b = e_phoff + i * e_phentsize
        p_type = struct.unpack_from("<I", data, b)[0]
        p_flags = struct.unpack_from("<I", data, b+4)[0]
        p_offset = struct.unpack_from("<Q", data, b+8)[0]
        p_vaddr  = struct.unpack_from("<Q", data, b+16)[0]
        p_filesz = struct.unpack_from("<Q", data, b+32)[0]
        p_memsz  = struct.unpack_from("<Q", data, b+40)[0]
        p_align  = struct.unpack_from("<Q", data, b+48)[0]
        name = NAMES.get(p_type, f"0x{p_type:x}")
        print(f"{i:>3}  {name:<20} 0x{p_offset:>12x} 0x{p_vaddr:>12x} 0x{p_filesz:>12x} 0x{p_memsz:>12x} 0x{p_align:>6x}")

if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else "/tmp/wta-test/libnode.from-device.so")
