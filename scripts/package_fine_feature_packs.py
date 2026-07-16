#!/usr/bin/env python3
import json
import shutil
import subprocess
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
AAR = ROOT / "feature-compat/build/outputs/aar/feature-compat-release.aar"
OUT_ROOT = ROOT / "app/src/main/assets/features"
LOCAL = ROOT / "local.properties"

PACKS = [
    {
        "id": "shell-forcedrun-hw",
        "prefixes": [
            "com/webtoapp/core/forcedrun/ForcedRunHardwareController",
            "com/webtoapp/core/forcedrun/NativeHardwareController",
        ],
        "entryClass": "com.webtoapp.core.forcedrun.ForcedRunHardwareController",
    },
    {
        "id": "ext-builtins",
        "prefixes": [
            "com/webtoapp/core/extension/BuiltInModules",
            "com/webtoapp/core/extension/BuiltInChromeExtensions",
            "com/webtoapp/core/extension/BrowserExtensionStore",
        ],
        "entryClass": "com.webtoapp.core.extension.BuiltInModules",
    },
    {
        "id": "ext-panel",
        "prefixes": [
            "com/webtoapp/core/extension/ExtensionPanelScript",
        ],
        "entryClass": "com.webtoapp.core.extension.ExtensionPanelScript",
    },
    {
        "id": "ext-chrome-scripts",
        "prefixes": [
            "com/webtoapp/core/extension/ChromeExtensionPolyfill",
            "com/webtoapp/core/extension/ChromeExtensionMobileCompat",
            "com/webtoapp/core/extension/UserScriptWindowScript",
        ],
        "entryClass": "com.webtoapp.core.extension.ChromeExtensionPolyfill",
    },
    {
        "id": "shell-disguise-js",
        "prefixes": [
            "com/webtoapp/core/appearance/BrowserDisguiseJsGenerator",
        ],
        "entryClass": "com.webtoapp.core.appearance.BrowserDisguiseJsGenerator",
    },
    {
        "id": "shell-translate-script",
        "prefixes": [
            "com/webtoapp/ui/shell/TranslateScriptProvider",
        ],
        "entryClass": "com.webtoapp.ui.shell.TranslateScriptProvider",
    },
    {
        "id": "shell-privacy",
        "prefixes": [
            "com/webtoapp/core/privacy/IsolationManager",
            "com/webtoapp/core/privacy/FingerprintGenerator",
            "com/webtoapp/core/privacy/GeneratedFingerprint",
            "com/webtoapp/core/privacy/IsolationScriptInjector",
        ],
        "entryClass": "com.webtoapp.core.privacy.IsolationManager",
    },
    {
        "id": "shell-error-games",
        "prefixes": [
            "com/webtoapp/core/errorpage/ErrorPageGames",
        ],
        "entryClass": "com.webtoapp.core.errorpage.ErrorPageGames",
    },
]


def sdk_d8() -> Path:
    props = {}
    for line in LOCAL.read_text().splitlines():
        if "=" in line and not line.strip().startswith("#"):
            k, v = line.split("=", 1)
            props[k.strip()] = v.strip()
    sdk = Path(props["sdk.dir"])
    tools = max((p for p in (sdk / "build-tools").iterdir() if p.is_dir()), key=lambda p: p.name)
    return tools / "d8"


def keep(name: str, prefixes) -> bool:
    for p in prefixes:
        if name == f"{p}.class" or name.startswith(f"{p}$") or name.startswith(f"{p}/"):
            return True
    return False


def filter_jar(src: Path, dest: Path, prefixes) -> int:
    count = 0
    dest.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(src, "r") as zin, zipfile.ZipFile(dest, "w") as zout:
        for info in zin.infolist():
            if not keep(info.filename, prefixes):
                continue
            zout.writestr(info, zin.read(info.filename))
            count += 1
    return count


def main() -> int:
    if not AAR.is_file():
        print(f"missing aar {AAR}", file=sys.stderr)
        return 1
    work = ROOT / "feature-compat/build/fine-pack-work"
    if work.exists():
        shutil.rmtree(work)
    work.mkdir(parents=True)
    with zipfile.ZipFile(AAR, "r") as z:
        z.extractall(work)
    classes = work / "classes.jar"
    if not classes.is_file():
        print("classes.jar missing", file=sys.stderr)
        return 1
    d8 = sdk_d8()
    OUT_ROOT.mkdir(parents=True, exist_ok=True)
    for pack in PACKS:
        filtered = work / pack["id"] / "classes.jar"
        n = filter_jar(classes, filtered, pack["prefixes"])
        if n == 0:
            print(f"skip {pack['id']}: no classes")
            continue
        dex_out = work / pack["id"] / "dex"
        dex_out.mkdir(parents=True, exist_ok=True)
        cmd = [str(d8), "--release", "--min-api", "23", "--output", str(dex_out), str(filtered)]
        proc = subprocess.run(cmd, capture_output=True, text=True)
        if proc.returncode != 0:
            print(f"d8 failed {pack['id']}: {proc.stdout}\n{proc.stderr}", file=sys.stderr)
            return proc.returncode
        out = OUT_ROOT / pack["id"]
        if out.exists():
            shutil.rmtree(out)
        out.mkdir(parents=True)
        dex_files = sorted(dex_out.glob("*.dex"))
        for d in dex_files:
            shutil.copy2(d, out / d.name)
        manifest = {
            "id": pack["id"],
            "version": 1,
            "minLiteApi": 1,
            "dependsOn": [],
            "entryClass": pack["entryClass"],
            "loadOrder": 10,
            "dex": [d.name for d in dex_files],
            "nativeLibs": [],
        }
        (out / "feature.json").write_text(json.dumps(manifest, indent=2) + "\n")
        kb = sum(d.stat().st_size for d in dex_files) // 1024
        print(f"packaged {pack['id']} classes={n} dex_kb={kb} -> {out}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
