#!/usr/bin/env python3
"""Print raw locale bytes for every config block in an ARSC.
Used to compare what AAPT2 emits vs. what we re-emit, byte-for-byte."""
import struct
import sys

RES_TABLE_PACKAGE = 0x0200
RES_TABLE_TYPE = 0x0201


def u16(d, o): return struct.unpack_from("<H", d, o)[0]
def u32(d, o): return struct.unpack_from("<I", d, o)[0]


def main(path):
    with open(path, 'rb') as f:
        data = f.read()
    pos = u16(data, 2)
    while pos + 8 <= len(data):
        ct = u16(data, pos)
        cs = u32(data, pos + 4)
        if cs == 0: break
        if ct == RES_TABLE_PACKAGE:
            walk_package(data, pos)
        pos += cs


def walk_package(data, off):
    chunk_size = u32(data, off + 4)
    end = off + chunk_size
    pos = off + u16(data, off + 2)
    type_strings_off = u32(data, off + 268)
    if type_strings_off:
        pos = max(pos, off + type_strings_off + u32(data, off + type_strings_off + 4))
    key_strings_off = u32(data, off + 276)
    if key_strings_off:
        pos = max(pos, off + key_strings_off + u32(data, off + key_strings_off + 4))
    seen = set()
    while pos + 8 <= end:
        ct = u16(data, pos)
        cs = u32(data, pos + 4)
        if cs == 0: break
        if ct == RES_TABLE_TYPE:
            cfg_size = u32(data, pos + 20)
            cfg = data[pos + 20:pos + 20 + cfg_size]
            # Bytes 8..12 are language[2] + country[2]; 36..48 may have script/variant
            key = bytes(cfg[8:12]) + b"|" + (bytes(cfg[36:40]) if len(cfg) >= 40 else b"") + b"|" + (bytes(cfg[40:48]) if len(cfg) >= 48 else b"")
            if key in seen:
                pos += cs
                continue
            seen.add(key)
            lang_bytes = cfg[8:12]
            script_bytes = cfg[36:40] if cfg_size >= 40 else b""
            variant_bytes = cfg[40:48] if cfg_size >= 48 else b""
            print(f"  cfg_size={cfg_size:3d}  lang/region={lang_bytes.hex():8s}  script={script_bytes.hex():8s}  variant={variant_bytes.hex():16s}  printable={lang_bytes!r}")
        pos += cs


if __name__ == '__main__':
    main(sys.argv[1])
