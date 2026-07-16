#!/usr/bin/env python3
import sys
import zipfile
from pathlib import Path

def should_skip(name: str) -> bool:
    if name.startswith("assets/dexopt/"):
        return True
    if name == "kotlin-tooling-metadata.json" or name.endswith("kotlin-tooling-metadata.json"):
        return True
    if name == "META-INF/version-control-info.textproto":
        return True
    if name == "META-INF/com/android/build/gradle/app-metadata.properties":
        return True
    if name.startswith("META-INF/") and name.endswith(".version"):
        return True
    if name.startswith("META-INF/") and name.endswith(".kotlin_module"):
        return True
    if name.startswith("META-INF/services/kotlinx.coroutines."):
        return True
    if name == "assets/php_router_server.php":
        return True
    if name.startswith("okhttp3/internal/publicsuffix/"):
        return True
    if name.endswith("/libcrypto_engine.so") or name.endswith("libcrypto_engine.so"):
        return True
    if name.endswith("/libbrowser_kernel.so") or name.endswith("libbrowser_kernel.so"):
        return True
    if name.endswith("/libc++_shared.so") or name.endswith("libc++_shared.so"):
        return True
    return False

def main():
    if len(sys.argv) < 2:
        print("usage: strip_shell_apk_bloat.py <apk>", file=sys.stderr)
        sys.exit(2)
    apk = Path(sys.argv[1])
    if not apk.is_file():
        print(f"missing {apk}", file=sys.stderr)
        sys.exit(1)
    tmp = apk.with_suffix(".stripped.apk")
    before = apk.stat().st_size
    removed = 0
    with zipfile.ZipFile(apk, "r") as zin, zipfile.ZipFile(tmp, "w") as zout:
        for info in zin.infolist():
            name = info.filename
            if should_skip(name):
                removed += 1
                continue
            data = zin.read(name)
            out = zipfile.ZipInfo(filename=name, date_time=info.date_time)
            out.compress_type = info.compress_type
            out.external_attr = info.external_attr
            out.create_system = info.create_system
            zout.writestr(out, data, compress_type=info.compress_type)
    tmp.replace(apk)
    after = apk.stat().st_size
    print(f"strip_apk before={before} after={after} saved={before-after} removed_entries={removed}")

if __name__ == "__main__":
    main()
