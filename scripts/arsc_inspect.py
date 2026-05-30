#!/usr/bin/env python3
"""Quick ARSC inspection tool."""
import struct
import sys

RES_STRING_POOL = 0x0001
RES_TABLE = 0x0002
RES_TABLE_PACKAGE = 0x0200
RES_TABLE_TYPE = 0x0201
RES_TABLE_TYPE_SPEC = 0x0202
ENTRY_FLAG_COMPLEX = 0x0001
ENTRY_FLAG_COMPACT = 0x0008
TYPE_FLAG_SPARSE = 0x01
TYPE_FLAG_OFFSET16 = 0x02


def u16(d, o): return struct.unpack_from("<H", d, o)[0]
def u32(d, o): return struct.unpack_from("<I", d, o)[0]


def parse_string_pool(d, off):
    header_size = u16(d, off + 2)
    string_count = u32(d, off + 8)
    flags = u32(d, off + 16)
    strings_start = u32(d, off + 20)
    is_utf8 = (flags & 0x100) != 0
    strings = []
    offsets_base = off + header_size
    strings_base = off + strings_start
    for i in range(string_count):
        so = u32(d, offsets_base + i * 4)
        abs_off = strings_base + so
        if is_utf8:
            cb1 = d[abs_off]
            o = abs_off + (2 if cb1 & 0x80 else 1)
            bb1 = d[o]
            if bb1 & 0x80:
                blen = ((bb1 & 0x7f) << 8) | d[o+1]
                o += 2
            else:
                blen = bb1
                o += 1
            s = d[o:o+blen].decode('utf-8', 'replace')
        else:
            cc = u16(d, abs_off)
            if cc & 0x8000:
                cc = ((cc & 0x7fff) << 16) | u16(d, abs_off + 2)
                ostart = abs_off + 4
            else:
                ostart = abs_off + 2
            blen = cc * 2
            s = d[ostart:ostart+blen].decode('utf-16-le', 'replace')
        strings.append(s)
    return strings


def main(path, target_id_hex=None):
    with open(path, 'rb') as f:
        data = f.read()
    target = int(target_id_hex, 16) if target_id_hex else None
    print(f"=== {path} ({len(data)} bytes) target={target_id_hex} ===")
    file_header_size = u16(data, 2)
    pos = file_header_size
    value_strings = []
    while pos + 8 <= len(data):
        ct = u16(data, pos)
        cs = u32(data, pos + 4)
        if cs == 0:
            break
        if ct == RES_STRING_POOL:
            value_strings = parse_string_pool(data, pos)
        elif ct == RES_TABLE_PACKAGE:
            walk_package(data, pos, value_strings, target)
        pos += cs


def walk_package(data, off, value_strings, target):
    chunk_size = u32(data, off + 4)
    pkg_id = u32(data, off + 8)
    type_strings_off = u32(data, off + 268)
    key_strings_off = u32(data, off + 276)
    type_names = parse_string_pool(data, off + type_strings_off) if type_strings_off else []
    key_names = parse_string_pool(data, off + key_strings_off) if key_strings_off else []
    pkg_end = off + chunk_size
    pos = off + u16(data, off + 2)
    if type_strings_off:
        pos = max(pos, off + type_strings_off + u32(data, off + type_strings_off + 4))
    if key_strings_off:
        pos = max(pos, off + key_strings_off + u32(data, off + key_strings_off + 4))
    while pos + 8 <= pkg_end:
        ct = u16(data, pos)
        cs = u32(data, pos + 4)
        if cs == 0:
            break
        if ct == RES_TABLE_TYPE:
            walk_type(data, pos, type_names, key_names, value_strings, target, pkg_id)
        pos += cs


def walk_type(data, off, type_names, key_names, value_strings, target, pkg_id):
    type_id = data[off + 8]
    type_flags = data[off + 9]
    entry_count = u32(data, off + 12)
    entries_start = u32(data, off + 16)
    config_size = u32(data, off + 20)
    config = data[off + 20:off + 20 + config_size]
    locale_l = (config[8], config[9]) if config_size >= 10 else (0, 0)
    locale_c = (config[10], config[11]) if config_size >= 12 else (0, 0)
    ui_mode_b = config[29] if config_size >= 30 else 0

    is_sparse = bool(type_flags & TYPE_FLAG_SPARSE)
    is_off16 = bool(type_flags & TYPE_FLAG_OFFSET16)
    header_size = u16(data, off + 2)
    offsets_base = off + header_size
    entries_abs_base = off + entries_start

    if target is None:
        return
    target_pkg = (target >> 24) & 0xff
    target_type = (target >> 16) & 0xff
    target_idx = target & 0xffff
    if target_type != type_id or target_pkg != pkg_id:
        return

    found = None
    if is_sparse:
        for i in range(entry_count):
            idx = u16(data, offsets_base + i * 4)
            o16 = u16(data, offsets_base + i * 4 + 2) * 4
            if idx == target_idx:
                found = entries_abs_base + o16
                break
    elif is_off16:
        if target_idx < entry_count:
            raw = u16(data, offsets_base + target_idx * 2)
            if raw != 0xffff:
                found = entries_abs_base + raw * 4
    else:
        if target_idx < entry_count:
            o32 = u32(data, offsets_base + target_idx * 4)
            if o32 != 0xffffffff:
                found = entries_abs_base + o32

    if found is None:
        return

    flags = u16(data, found + 2)
    is_complex = bool(flags & ENTRY_FLAG_COMPLEX)
    key_idx = u32(data, found + 4)
    key = key_names[key_idx] if key_idx < len(key_names) else f"#{key_idx}"
    loc = ""
    if locale_l[0]: loc += chr(locale_l[0]) + chr(locale_l[1])
    if locale_c[0]: loc += "-" + chr(locale_c[0]) + chr(locale_c[1])
    print(f"  type=0x{type_id:02x} key={key} loc={loc or '(none)'} ui_byte=0x{ui_mode_b:02x} cflags=0x{flags:04x} {'COMPLEX' if is_complex else 'SIMPLE'}")

    if is_complex:
        parent = u32(data, found + 8)
        cnt = u32(data, found + 12)
        print(f"    parent=0x{parent:08x} entries={cnt}")
        # Look for the offending nameRef
        for i in range(min(cnt, 8)):
            mo = found + 16 + i * 12
            nr = u32(data, mo)
            vs = u16(data, mo + 4)
            vdt = data[mo + 4 + 3]
            vd = u32(data, mo + 4 + 4)
            print(f"    [{i}] nameRef=0x{nr:08x} dt=0x{vdt:02x} data=0x{vd:08x}")
    else:
        vdt = data[found + 8 + 3]
        vd = u32(data, found + 8 + 4)
        print(f"    res_value: dt=0x{vdt:02x} data=0x{vd:08x}")


if __name__ == '__main__':
    target = sys.argv[2] if len(sys.argv) > 2 else None
    main(sys.argv[1], target)
