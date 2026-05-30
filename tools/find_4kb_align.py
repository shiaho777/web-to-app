#!/usr/bin/env python3
"""Find any phdr with p_align = 0x1000 (4096) to identify what bionic might be rejecting."""
import struct
import sys

def main(path):
    with open(path, "rb") as f:
        data = f.read()
    e_phoff = struct.unpack_from("<Q", data, 32)[0]
    e_phentsize = struct.unpack_from("<H", data, 54)[0]
    e_phnum = struct.unpack_from("<H", data, 56)[0]
    print(f"e_phnum={e_phnum} e_phentsize={e_phentsize}")
    for i in range(e_phnum):
        b = e_phoff + i * e_phentsize
        p_type = struct.unpack_from("<I", data, b)[0]
        p_align = struct.unpack_from("<Q", data, b + 48)[0]
        if p_align == 0x1000 or p_align == 4096:
            print(f"  ### FOUND 4KB align at phdr #{i}: type=0x{p_type:x}")
        else:
            print(f"  phdr #{i}: type=0x{p_type:x} align=0x{p_align:x}")

if __name__ == "__main__":
    main(sys.argv[1])
