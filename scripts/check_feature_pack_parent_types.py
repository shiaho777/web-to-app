#!/usr/bin/env python3
import argparse
import struct
import sys
import zipfile
from pathlib import Path


FRAMEWORK_PREFIXES = (
    "Ljava/",
    "Ljavax/",
    "Landroid/",
    "Ldalvik/",
    "Lorg/json/",
    "Lorg/xmlpull/",
    "Lorg/w3c/",
    "Lorg/xml/",
    "Llibcore/",
    "Lsun/",
    "Lcom/android/",
)

IGNORE_SUBSTR = (
    "errorprone",
    "checkerframework",
    "jspecify",
    "animal_sniffer",
    "j2objc",
    "flogger",
    "snakeyaml",
    "/R$",
    "/R;",
    "kotlin/annotations/",
    "org/bouncycastle/",
    "org/intellij/",
    "org/jetbrains/annotations/",
)

DEFAULT_ALLOW = {
    "Landroidx/legacy/content/WakefulBroadcastReceiver;",
}


def uleb(buf: bytes, off: int):
    result = 0
    shift = 0
    while True:
        b = buf[off]
        off += 1
        result |= (b & 0x7F) << shift
        if (b & 0x80) == 0:
            break
        shift += 7
    return result, off


def parse_dex(data: bytes):
    if len(data) < 0x70 or data[:3] != b"dex":
        return set(), set()
    string_ids_size, string_ids_off = struct.unpack_from("<II", data, 0x38)
    type_ids_size, type_ids_off = struct.unpack_from("<II", data, 0x40)
    class_defs_size, class_defs_off = struct.unpack_from("<II", data, 0x60)
    strings = []
    for i in range(string_ids_size):
        soff = struct.unpack_from("<I", data, string_ids_off + i * 4)[0]
        _, p = uleb(data, soff)
        end = data.index(b"\x00", p)
        strings.append(data[p:end].decode("utf-8", "replace"))
    types = [
        strings[struct.unpack_from("<I", data, type_ids_off + i * 4)[0]]
        for i in range(type_ids_size)
    ]
    classes = {
        types[struct.unpack_from("<I", data, class_defs_off + i * 0x20)[0]]
        for i in range(class_defs_size)
    }
    return set(types), classes


def load_dex_bytes_from_apk(apk: Path):
    out = []
    with zipfile.ZipFile(apk) as z:
        for name in z.namelist():
            if name.startswith("classes") and name.endswith(".dex"):
                out.append(z.read(name))
    return out


def load_dex_bytes_from_dir(directory: Path):
    return [p.read_bytes() for p in sorted(directory.glob("classes*.dex")) if p.is_file()]


def merge_dex(dex_list):
    types = set()
    classes = set()
    for data in dex_list:
        t, c = parse_dex(data)
        types |= t
        classes |= c
    return types, classes


def is_framework(t: str) -> bool:
    if not t.startswith("L"):
        return True
    if t.startswith("["):
        return True
    return any(t.startswith(p) for p in FRAMEWORK_PREFIXES)


def is_ignored(t: str) -> bool:
    return any(s in t for s in IGNORE_SUBSTR)


def collect_pack_dirs(features_dir: Path):
    packs = []
    for child in sorted(features_dir.iterdir()):
        if not child.is_dir():
            continue
        if list(child.glob("classes*.dex")):
            packs.append(child)
    return packs


def main():
    parser = argparse.ArgumentParser(
        description="Verify feature pack DEX parent types resolve in LITE shell template"
    )
    parser.add_argument(
        "--shell-apk",
        type=Path,
        default=Path("app/src/main/assets/template/webview_shell.apk"),
    )
    parser.add_argument(
        "--features-dir",
        type=Path,
        default=Path("app/src/main/assets/features"),
    )
    parser.add_argument("--allow", action="append", default=[], help="Extra allowed missing type descriptor")
    parser.add_argument("--warn-only", action="store_true")
    parser.add_argument("--verbose", action="store_true")
    args = parser.parse_args()

    if not args.shell_apk.is_file():
        print(f"missing shell apk: {args.shell_apk}", file=sys.stderr)
        return 1
    if not args.features_dir.is_dir():
        print(f"missing features dir: {args.features_dir}", file=sys.stderr)
        return 1

    shell_types, shell_classes = merge_dex(load_dex_bytes_from_apk(args.shell_apk))
    allow = set(DEFAULT_ALLOW)
    allow.update(args.allow)

    packs = collect_pack_dirs(args.features_dir)
    if not packs:
        print(f"no feature packs under {args.features_dir}", file=sys.stderr)
        return 1

    print(
        f"shell={args.shell_apk} classes={len(shell_classes)} "
        f"size={args.shell_apk.stat().st_size}"
    )

    total_missing = 0
    pack_reports = []
    for pack in packs:
        pack_types, pack_classes = merge_dex(load_dex_bytes_from_dir(pack))
        missing = []
        for t in sorted(pack_types):
            if is_framework(t) or is_ignored(t):
                continue
            if t in pack_classes or t in shell_classes:
                continue
            if t in allow:
                continue
            missing.append(t)
        pack_reports.append((pack.name, len(pack_classes), missing))
        total_missing += len(missing)
        status = "OK" if not missing else f"FAIL {len(missing)}"
        print(f"  {pack.name}: classes={len(pack_classes)} {status}")
        if missing:
            show = missing if args.verbose else missing[:20]
            for t in show:
                print(f"    {t}")
            if not args.verbose and len(missing) > 20:
                print(f"    ... +{len(missing) - 20} more")

    critical = [
        "Lkotlin/jvm/internal/Intrinsics;",
        "Lkotlin/Pair;",
        "Lkotlinx/coroutines/Dispatchers;",
        "Lcom/google/gson/Gson;",
        "Lokhttp3/OkHttpClient;",
        "Lcom/webtoapp/core/logging/AppLogger;",
        "Lcom/webtoapp/core/extension/ExtensionModule;",
        "Lcom/webtoapp/core/privacy/IsolationConfig;",
        "Lcom/webtoapp/core/extension/ExtensionPanelScript;",
    ]
    shell_miss = [t for t in critical if t not in shell_classes and t != "Lcom/webtoapp/core/extension/ExtensionPanelScript;"]
    pack_only_ok = "Lcom/webtoapp/core/extension/ExtensionPanelScript;"
    panel_in_shell = pack_only_ok in shell_classes
    panel_in_any_pack = any(
        pack_only_ok in merge_dex(load_dex_bytes_from_dir(p))[1] for p in packs
    )
    print("critical shell FQCNs:")
    for t in critical:
        if t == pack_only_ok:
            where = "shell" if panel_in_shell else ("pack" if panel_in_any_pack else "MISSING")
            print(f"  {t} -> {where}")
        else:
            print(f"  {t} -> {'OK' if t in shell_classes else 'MISS'}")

    failed = total_missing > 0 or len(shell_miss) > 0 or not panel_in_any_pack
    if failed:
        print(
            f"check_feature_pack_parent_types: unresolved={total_missing} "
            f"critical_shell_miss={len(shell_miss)} panel_pack={'yes' if panel_in_any_pack else 'no'}",
            file=sys.stderr,
        )
        return 0 if args.warn_only else 1

    print("check_feature_pack_parent_types: OK")
    return 0


if __name__ == "__main__":
    sys.exit(main())
