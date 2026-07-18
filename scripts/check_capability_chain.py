#!/usr/bin/env python3
import argparse
import re
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]

SOFT_LOAD_CLASSES = {
    "com.webtoapp.core.privacy.IsolationManager": ("shell-privacy", "feature-compat"),
    "com.webtoapp.core.privacy.FingerprintGenerator": ("shell-privacy", "feature-compat"),
    "com.webtoapp.core.privacy.IsolationScriptInjector": ("shell-privacy", "feature-compat"),
    "com.webtoapp.core.forcedrun.ForcedRunHardwareController": ("shell-forcedrun-hw", "feature-compat"),
    "com.webtoapp.core.forcedrun.NativeHardwareController": ("shell-forcedrun-hw", "feature-compat"),
    "com.webtoapp.core.engine.GeckoViewEngine": ("feature-compat",),
    "com.webtoapp.core.notification.NotificationFcmManager": ("feature-compat",),
    "com.webtoapp.core.webview.TlsMitmBridge": ("feature-compat",),
    "com.webtoapp.core.webview.TlsMitmCaManager": ("feature-compat",),
    "com.webtoapp.core.errorpage.ErrorPageGames": ("shell-error-games", "feature-compat"),
    "com.webtoapp.core.extension.ExtensionPanelScript": ("ext-panel", "feature-compat"),
    "com.webtoapp.core.extension.BuiltInModules": ("ext-builtins", "feature-compat"),
    "com.webtoapp.core.extension.ChromeExtensionPolyfill": ("ext-chrome-scripts", "feature-compat"),
    "com.webtoapp.core.extension.ChromeExtensionMobileCompat": ("ext-chrome-scripts", "feature-compat"),
    "com.webtoapp.core.extension.UserScriptWindowScript": ("ext-chrome-scripts", "feature-compat"),
    "com.webtoapp.core.appearance.BrowserDisguiseJsGenerator": ("shell-disguise-js", "feature-compat"),
    "com.webtoapp.ui.shell.TranslateScriptProvider": ("shell-translate-script", "feature-compat"),
}

SHELL_HEAVY_MUST_PACK = [
    "**/core/engine/GeckoViewEngine.kt",
    "**/core/notification/NotificationFcmManager.kt",
    "**/core/webview/TlsMitmBridge.kt",
    "**/core/webview/TlsMitmCaManager.kt",
    "**/core/extension/BuiltInModules.kt",
    "**/core/extension/ExtensionPanelScript.kt",
    "**/core/extension/ChromeExtensionPolyfill.kt",
    "**/core/extension/ChromeExtensionMobileCompat.kt",
    "**/core/extension/UserScriptWindowScript.kt",
    "**/core/appearance/BrowserDisguiseJsGenerator.kt",
    "**/ui/shell/TranslateScriptProvider.kt",
    "**/core/forcedrun/ForcedRunHardwareController.kt",
    "**/core/forcedrun/NativeHardwareController.kt",
    "**/core/privacy/IsolationManager.kt",
    "**/core/privacy/FingerprintGenerator.kt",
    "**/core/privacy/IsolationScriptInjector.kt",
    "**/core/errorpage/ErrorPageGames.kt",
]

ACCESS_CLASS_LITERALS = [
    ("app/src/main/java/com/webtoapp/core/privacy/IsolationPrivacyAccess.kt", "com.webtoapp.core.privacy.IsolationManager"),
    ("app/src/main/java/com/webtoapp/core/forcedrun/ForcedRunHardwareAccess.kt", "com.webtoapp.core.forcedrun.ForcedRunHardwareController"),
    ("app/src/main/java/com/webtoapp/core/engine/GeckoEngineAccess.kt", "com.webtoapp.core.engine.GeckoViewEngine"),
    ("app/src/main/java/com/webtoapp/core/notification/FcmAccess.kt", "com.webtoapp.core.notification.NotificationFcmManager"),
    ("app/src/main/java/com/webtoapp/core/webview/TlsMitmAccess.kt", "com.webtoapp.core.webview.TlsMitmBridge"),
]


def parse_fine_pack_ids(script: Path) -> list[str]:
    text = script.read_text(encoding="utf-8")
    return re.findall(r'"id"\s*:\s*"([^"]+)"', text)


def extract_quoted_globs(text: str, marker: str) -> set[str]:
    idx = text.find(marker)
    if idx < 0:
        return set()
    chunk = text[idx : idx + 8000]
    return set(re.findall(r'"(\*\*/[^"]+)"', chunk))


def pack_dex_blob(pack_dir: Path) -> bytes:
    blobs = []
    for dex in sorted(pack_dir.glob("*.dex")):
        blobs.append(dex.read_bytes())
    return b"".join(blobs)


def class_present(blob: bytes, fqcn: str) -> bool:
    slash = fqcn.replace(".", "/").encode("utf-8")
    simple = fqcn.rsplit(".", 1)[-1].encode("utf-8")
    return slash in blob or simple in blob


def main() -> int:
    parser = argparse.ArgumentParser(description="Validate LITE cut + feature pack capability chain")
    parser.add_argument(
        "--features-dir",
        type=Path,
        default=ROOT / "app/src/main/assets/features",
    )
    parser.add_argument(
        "--fine-pack-script",
        type=Path,
        default=ROOT / "scripts/package_fine_feature_packs.py",
    )
    parser.add_argument(
        "--shell-gradle",
        type=Path,
        default=ROOT / "shell/build.gradle.kts",
    )
    parser.add_argument(
        "--feature-compat-gradle",
        type=Path,
        default=ROOT / "feature-compat/build.gradle.kts",
    )
    args = parser.parse_args()

    errors: list[str] = []
    warnings: list[str] = []

    if not args.features_dir.is_dir():
        errors.append(f"features dir missing: {args.features_dir}")
        print("\n".join(errors))
        return 1

    fine_ids = parse_fine_pack_ids(args.fine_pack_script)
    if not fine_ids:
        errors.append(f"no fine pack ids parsed from {args.fine_pack_script}")

    asset_ids = sorted(
        p.name for p in args.features_dir.iterdir() if p.is_dir() and not p.name.startswith(".")
    )
    required_ids = set(fine_ids) | {"feature-compat"}

    for pack_id in sorted(required_ids):
        pack_dir = args.features_dir / pack_id
        if not pack_dir.is_dir():
            errors.append(f"missing pack directory: features/{pack_id}")
            continue
        if not (pack_dir / "feature.json").is_file():
            errors.append(f"missing feature.json: features/{pack_id}/feature.json")
        dex_files = list(pack_dir.glob("*.dex"))
        if not dex_files:
            errors.append(f"missing classes.dex: features/{pack_id}/")

    extra_asset_ids = set(asset_ids) - required_ids
    if extra_asset_ids:
        warnings.append(f"extra feature pack dirs not declared in fine pack script: {sorted(extra_asset_ids)}")

    pack_blobs: dict[str, bytes] = {}
    for pack_id in asset_ids:
        pack_blobs[pack_id] = pack_dex_blob(args.features_dir / pack_id)

    for fqcn, allowed_packs in SOFT_LOAD_CLASSES.items():
        found_in = [pid for pid in allowed_packs if class_present(pack_blobs.get(pid, b""), fqcn)]
        if not found_in:
            present_elsewhere = [pid for pid, blob in pack_blobs.items() if class_present(blob, fqcn)]
            if present_elsewhere:
                errors.append(
                    f"soft-load class {fqcn} not in expected packs {list(allowed_packs)}; "
                    f"found in {present_elsewhere}"
                )
            else:
                errors.append(
                    f"soft-load class {fqcn} missing from packs {list(allowed_packs)}"
                )

    for rel, fqcn in ACCESS_CLASS_LITERALS:
        path = ROOT / rel
        if not path.is_file():
            errors.append(f"Access helper missing: {rel}")
            continue
        text = path.read_text(encoding="utf-8")
        if fqcn not in text:
            errors.append(f"{rel} does not reference soft-load FQCN {fqcn}")

    script_access = ROOT / "feature-api/src/main/java/com/webtoapp/core/feature/ScriptPackAccess.kt"
    if script_access.is_file():
        text = script_access.read_text(encoding="utf-8")
        for fqcn in (
            "com.webtoapp.core.extension.ChromeExtensionPolyfill",
            "com.webtoapp.core.extension.ChromeExtensionMobileCompat",
            "com.webtoapp.core.extension.UserScriptWindowScript",
            "com.webtoapp.core.appearance.BrowserDisguiseJsGenerator",
        ):
            if fqcn not in text:
                errors.append(f"ScriptPackAccess missing FQCN {fqcn}")
    else:
        errors.append("ScriptPackAccess.kt missing")

    shell_text = args.shell_gradle.read_text(encoding="utf-8") if args.shell_gradle.is_file() else ""
    compat_text = (
        args.feature_compat_gradle.read_text(encoding="utf-8")
        if args.feature_compat_gradle.is_file()
        else ""
    )
    if not shell_text:
        errors.append(f"shell gradle missing: {args.shell_gradle}")
    if not compat_text:
        errors.append(f"feature-compat gradle missing: {args.feature_compat_gradle}")

    shell_excludes = extract_quoted_globs(shell_text, "exclude(")
    if not shell_excludes:
        shell_excludes = set(re.findall(r'"(\*\*/core/[^"]+\.kt)"', shell_text))
    compat_includes = extract_quoted_globs(compat_text, "include(")
    if not compat_includes:
        compat_includes = set(re.findall(r'"(\*\*/[^"]+\.kt)"', compat_text))

    for pattern in SHELL_HEAVY_MUST_PACK:
        if pattern not in shell_excludes and pattern not in shell_text:
            errors.append(f"shell exclude missing heavy runtime: {pattern}")
        if pattern not in compat_includes and pattern not in compat_text:
            errors.append(f"feature-compat include missing heavy runtime: {pattern}")

    planner = ROOT / "app/src/main/java/com/webtoapp/core/apkbuilder/CapabilityPlanner.kt"
    if planner.is_file():
        ptext = planner.read_text(encoding="utf-8")
        for token in (
            "GRANULAR_PACK_IDS",
            "materializePackIds",
            "closeDependencies",
            "FeatureIds.SHELL_PRIVACY",
            "FeatureIds.EXT_MODULES",
            "FeatureIds.SHELL_ERROR_GAMES",
        ):
            if token not in ptext:
                errors.append(f"CapabilityPlanner missing {token}")
    else:
        errors.append("CapabilityPlanner.kt missing")

    builder = ROOT / "app/src/main/java/com/webtoapp/core/apkbuilder/ApkBuilder.kt"
    if builder.is_file():
        btext = builder.read_text(encoding="utf-8")
        if "Missing feature packs required by capability plan" not in btext:
            errors.append("ApkBuilder does not hard-fail on missing feature packs")
    else:
        errors.append("ApkBuilder.kt missing")

    merger = ROOT / "app/src/main/java/com/webtoapp/core/apkbuilder/FeaturePackMerger.kt"
    if merger.is_file():
        mtext = merger.read_text(encoding="utf-8")
        if "missingFeatures" not in mtext:
            errors.append("FeaturePackMerger missing missingFeatures reporting")
    else:
        errors.append("FeaturePackMerger.kt missing")

    feature_ids = ROOT / "feature-api/src/main/java/com/webtoapp/core/feature/FeatureIds.kt"
    if feature_ids.is_file():
        ftext = feature_ids.read_text(encoding="utf-8")
        for pack_id in sorted(required_ids):
            if f'"{pack_id}"' not in ftext and pack_id != "feature-compat":
                if pack_id not in ftext:
                    warnings.append(f"FeatureIds may not declare pack id {pack_id}")
            if pack_id == "feature-compat" and "feature-compat" not in ftext:
                errors.append("FeatureIds missing feature-compat")
    else:
        errors.append("FeatureIds.kt missing")

    if warnings:
        for w in warnings:
            print(f"WARN: {w}")

    if errors:
        for e in errors:
            print(f"ERROR: {e}")
        print(f"check_capability_chain: FAILED ({len(errors)} errors)")
        return 1

    print(
        "check_capability_chain: OK "
        f"(packs={len(required_ids)} soft-load={len(SOFT_LOAD_CLASSES)} heavies={len(SHELL_HEAVY_MUST_PACK)})"
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
