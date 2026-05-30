#!/usr/bin/env python3
"""Dump every ResTable_type's config bytes (locale-relevant range) in an
ARSC, decoded with my best-effort parser. Used to compare what locales
the original AAPT2-emitted ARSC encodes vs. what our reader sees."""
import struct
import sys

RES_STRING_POOL = 0x0001
RES_TABLE = 0x0002
RES_TABLE_PACKAGE = 0x0200
RES_TABLE_TYPE = 0x0201


def u16(d, o): return struct.unpack_from("<H", d, o)[0]
def u32(d, o): return struct.unpack_from("<I", d, o)[0]


def parse_string_pool(d, off):
    header_size = u16(d, off + 2)
    string_count = u32(d, off + 8)
    flags = u32(d, off + 16)
    strings_start = u32(d, off + 20)
    is_utf8 = (flags & 0x100) != 0
    out = []
    ob = off + header_size
    sb = off + strings_start
    for i in range(string_count):
        so = u32(d, ob + i*4)
        ao = sb + so
        if is_utf8:
            cb = d[ao]
            o = ao + (2 if cb & 0x80 else 1)
            bb = d[o]
            if bb & 0x80:
                bl = ((bb & 0x7f) << 8) | d[o+1]
                o += 2
            else:
                bl = bb
                o += 1
            s = d[o:o+bl].decode('utf-8', 'replace')
        else:
            cc = u16(d, ao)
            if cc & 0x8000:
                cc = ((cc & 0x7fff) << 16) | u16(d, ao+2)
                ostart = ao + 4
            else:
                ostart = ao + 2
            bl = cc * 2
            s = d[ostart:ostart+bl].decode('utf-16-le', 'replace')
        out.append(s)
    return out


def unpack_lang_or_region(b0, b1, base):
    """Decode 2-byte ResTable_config language/country to text.
       base is 'a' for lang, '0' for region."""
    if b0 == 0:
        return ""
    if (b0 & 0x80) == 0:
        # plain ASCII
        return chr(b0) + chr(b1)
    # packed form: 5-bit per char
    base_ord = ord(base)
    c0 = ((b0 & 0x7c) >> 2) + base_ord
    c1 = (((b0 & 0x03) << 3) | ((b1 & 0xe0) >> 5)) + base_ord
    c2 = (b1 & 0x1f) + base_ord
    return chr(c0) + chr(c1) + chr(c2)


def decode_config(cfg):
    if len(cfg) < 12:
        return "(default)"
    lang = unpack_lang_or_region(cfg[8], cfg[9], 'a') if cfg[8] else ""
    region = unpack_lang_or_region(cfg[10], cfg[11], '0') if cfg[10] else ""
    # Script: 4 bytes at offset 36 (only present if size >= 40)
    script = ""
    if len(cfg) >= 40:
        if cfg[36] != 0:
            script = bytes(cfg[36:40]).rstrip(b'\x00').decode('ascii', 'replace')
    variant = ""
    if len(cfg) >= 48:
        if cfg[40] != 0:
            variant = bytes(cfg[40:48]).rstrip(b'\x00').decode('ascii', 'replace')

    parts = []
    if lang:
        if script and not region:
            parts.append("b+" + lang + "+" + script)
        elif lang:
            parts.append(lang)
            if region:
                parts.append("r" + region)
            if script:
                parts.append("+" + script)
            if variant:
                parts.append("+" + variant)
    return "-".join(parts) if parts else "(default)"


def walk_package(data, off):
    chunk_size = u32(data, off + 4)
    type_strings_off = u32(data, off + 268)
    key_strings_off = u32(data, off + 276)
    type_names = parse_string_pool(data, off + type_strings_off) if type_strings_off else []
    pos = off + u16(data, off + 2)
    if type_strings_off:
        pos = max(pos, off + type_strings_off + u32(data, off + type_strings_off + 4))
    if key_strings_off:
        pos = max(pos, off + key_strings_off + u32(data, off + key_strings_off + 4))
    seen_locales = {}
    end = off + chunk_size
    while pos + 8 <= end:
        ct = u16(data, pos)
        cs = u32(data, pos + 4)
        if cs == 0: break
        if ct == RES_TABLE_TYPE:
            type_id = data[pos + 8]
            tn = type_names[type_id-1] if type_id-1 < len(type_names) else f"#{type_id}"
            config_size = u32(data, pos + 20)
            cfg = data[pos + 20:pos + 20 + config_size]
            loc = decode_config(cfg)
            if loc != "(default)":
                seen_locales.setdefault(loc, set()).add(tn)
        pos += cs
    return seen_locales


def main(path):
    with open(path, 'rb') as f:
        data = f.read()
    pos = u16(data, 2)
    while pos + 8 <= len(data):
        ct = u16(data, pos)
        cs = u32(data, pos + 4)
        if cs == 0: break
        if ct == RES_TABLE_PACKAGE:
            locs = walk_package(data, pos)
            for loc in sorted(locs):
                types = sorted(locs[loc])
                print(f"  {loc:30s}  types: {','.join(types[:6])}{'...' if len(types) > 6 else ''}")
        pos += cs


if __name__ == '__main__':
    main(sys.argv[1])
