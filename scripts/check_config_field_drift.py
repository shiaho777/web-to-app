#!/usr/bin/env python3
import argparse
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

PAYLOAD_FILE = ROOT / "app/src/main/java/com/webtoapp/core/apkbuilder/ApkConfigJsonFactory.kt"
APKCONFIG_FILE = ROOT / "app/src/main/java/com/webtoapp/core/apkbuilder/ApkConfig.kt"
SHELLCONFIG_FILE = ROOT / "app/src/main/java/com/webtoapp/core/shell/ShellModeManager.kt"
ALLOWLIST_FILE = ROOT / "scripts/config_field_drift_allowlist.json"

OBJECT_RETURN_PAYLOAD_CLASSES = ("BackgroundRunConfig", "NotificationConfig")

PAYLOAD_FUNCS = [
    "toShellPayload",
    "webViewConfigPayload",
    "floatingWindowConfigPayload",
    "errorPageConfigPayload",
    "mediaConfigPayload",
    "htmlConfigPayload",
    "galleryConfigPayload",
    "autoStartConfigPayload",
    "isolationConfigPayload",
    "backgroundRunConfigPayload",
    "notificationConfigPayload",
    "networkTrustConfigPayload",
    "wordpressConfigPayload",
    "nodejsConfigPayload",
    "phpAppConfigPayload",
    "pythonAppConfigPayload",
    "goAppConfigPayload",
    "multiWebConfigPayload",
]

PAIR_KEY = re.compile(r'"([A-Za-z][A-Za-z0-9_]*)"\s+to\b')
SERIAL_NAME = re.compile(r'@SerializedName\(\s*"([A-Za-z][A-Za-z0-9_]*)"\s*\)')
FUN_HEADER = re.compile(r"fun\s+ApkConfig\.(\w+)\s*\(\s*\)\s*:\s*([A-Za-z<>?,\s]+)\s*[={]")


def extract_payload_keys(text: str) -> set[str]:
    lines = text.splitlines()
    func_ranges: list[tuple[str, int, int]] = []
    headers: list[tuple[int, str, str]] = []
    for idx, line in enumerate(lines):
        m = FUN_HEADER.search(line)
        if not m:
            continue
        name = m.group(1)
        if not name.endswith("Payload"):
            continue
        headers.append((idx, name, m.group(2)))

    for i, (idx, name, _ret) in enumerate(headers):
        end = headers[i + 1][0] if i + 1 < len(headers) else len(lines)
        func_ranges.append((name, idx, end))

    keys: set[str] = set()
    for name, start, end in func_ranges:
        if name not in PAYLOAD_FUNCS:
            continue
        body = "\n".join(lines[start:end])
        keys.update(PAIR_KEY.findall(body))
    return keys


def extract_object_returned_keys(text: str) -> set[str]:
    keys: set[str] = set()
    for cls in OBJECT_RETURN_PAYLOAD_CLASSES:
        pattern = re.compile(rf"data class {cls}\s*\((.*?)\n\)", re.DOTALL)
        m = pattern.search(text)
        if not m:
            continue
        body = m.group(1)
        for prop in re.finditer(r"^\s*val\s+(\w+)\s*:", body, re.MULTILINE):
            keys.add(prop.group(1))
    return keys


def extract_shell_keys(text: str) -> set[str]:
    return set(SERIAL_NAME.findall(text))


def load_allowlist() -> tuple[set[str], dict[str, str]]:
    if not ALLOWLIST_FILE.is_file():
        return set(), {}
    data = json.loads(ALLOWLIST_FILE.read_text(encoding="utf-8"))
    entries = data.get("allowlist", [])
    keys: set[str] = set()
    reasons: dict[str, str] = {}
    for entry in entries:
        key = entry.get("key")
        reason = (entry.get("reason") or "").strip()
        if not key:
            continue
        keys.add(key)
        reasons[key] = reason
    return keys, reasons


def main() -> int:
    parser = argparse.ArgumentParser(description="Detect field-name drift between ApkConfig payload and ShellConfig @SerializedName")
    parser.add_argument("--payload-file", type=Path, default=PAYLOAD_FILE)
    parser.add_argument("--apkconfig-file", type=Path, default=APKCONFIG_FILE)
    parser.add_argument("--shellconfig-file", type=Path, default=SHELLCONFIG_FILE)
    parser.add_argument("--allowlist-file", type=Path, default=ALLOWLIST_FILE)
    args = parser.parse_args()

    errors: list[str] = []
    warnings: list[str] = []

    for f in (args.payload_file, args.apkconfig_file, args.shellconfig_file):
        if not f.is_file():
            errors.append(f"source file missing: {f}")

    if errors:
        for e in errors:
            print(f"ERROR: {e}")
        return 1

    payload_text = args.payload_file.read_text(encoding="utf-8")
    apkconfig_text = args.apkconfig_file.read_text(encoding="utf-8")
    shell_text = args.shellconfig_file.read_text(encoding="utf-8")

    payload_keys = extract_payload_keys(payload_text)
    payload_keys |= extract_object_returned_keys(apkconfig_text)
    shell_keys = extract_shell_keys(shell_text)

    allow_keys, allow_reasons = load_allowlist()

    allow_no_reason = sorted(k for k in allow_keys if not allow_reasons.get(k))
    if allow_no_reason:
        errors.append(
            "allowlist entries missing 'reason' (every entry must document why it is exempt): "
            + ", ".join(allow_no_reason)
        )

    stale_allow = sorted(k for k in allow_keys if k not in payload_keys and k not in shell_keys)
    if stale_allow:
        warnings.append(
            "allowlist contains keys present in neither side (stale, remove them): "
            + ", ".join(stale_allow)
        )

    only_payload = sorted((payload_keys - shell_keys) - allow_keys)
    only_shell = sorted((shell_keys - payload_keys) - allow_keys)

    for k in only_payload:
        errors.append(
            f"payload writes key '{k}' but ShellConfig has no matching @SerializedName "
            "(serialized but runtime never receives)"
        )
    for k in only_shell:
        errors.append(
            f"ShellConfig declares @SerializedName('{k}') but no payload writes it "
            "(runtime field exists but export never emits)"
        )

    if warnings:
        for w in warnings:
            print(f"WARN: {w}")

    if errors:
        for e in errors:
            print(f"ERROR: {e}")
        print(
            f"check_config_field_drift: FAILED "
            f"(payload_only={len(only_payload)} shell_only={len(only_shell)} "
            f"allowlist={len(allow_keys)})"
        )
        return 1

    print(
        f"check_config_field_drift: OK "
        f"(payload={len(payload_keys)} shell={len(shell_keys)} "
        f"allowlist={len(allow_keys)} shared={len(payload_keys & shell_keys)})"
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
