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
MODEL_FILE = ROOT / "app/src/main/java/com/webtoapp/data/model/WebApp.kt"
ALLOWLIST_FILE = ROOT / "scripts/config_field_drift_allowlist.json"

OBJECT_RETURN_PAYLOAD_CLASSES = (
    "BackgroundRunConfig",
    "NotificationConfig",
    "GalleryShellItemConfig",
    "BgmShellItem",
    "LrcShellTheme",
)

PAYLOAD_FUNCS = [
    ("ApkConfig", "toShellPayload"),
    ("ApkConfig", "webViewConfigPayload"),
    ("ApkConfig", "floatingWindowConfigPayload"),
    ("ApkConfig", "errorPageConfigPayload"),
    ("ApkConfig", "mediaConfigPayload"),
    ("ApkConfig", "htmlConfigPayload"),
    ("ApkConfig", "galleryConfigPayload"),
    ("ApkConfig", "autoStartConfigPayload"),
    ("ApkConfig", "isolationConfigPayload"),
    ("ApkConfig", "backgroundRunConfigPayload"),
    ("ApkConfig", "notificationConfigPayload"),
    ("ApkConfig", "networkTrustConfigPayload"),
    ("ApkConfig", "wordpressConfigPayload"),
    ("ApkConfig", "nodejsConfigPayload"),
    ("ApkConfig", "phpAppConfigPayload"),
    ("ApkConfig", "pythonAppConfigPayload"),
    ("ApkConfig", "goAppConfigPayload"),
    ("ApkConfig", "multiWebConfigPayload"),
    ("EmbeddedPlugin", "toPayload"),
]

PAIR_KEY = re.compile(r'"([A-Za-z][A-Za-z0-9_]*)"\s+to\b')
SERIAL_NAME = re.compile(r'@SerializedName\(\s*"([A-Za-z][A-Za-z0-9_]*)"\s*\)')
FUN_HEADER = re.compile(r"\bfun\s+([A-Za-z_]\w*)\.(\w+)\s*\(\s*\)\s*(?::|\{)")
ANY_FUN = re.compile(r"\bfun\s+(?:[A-Za-z_]\w*\.)?\w+\s*\(")
SHARED_TYPE_REF = re.compile(r"\bcom\.webtoapp\.data\.model\.([A-Za-z_]\w*)")
DATA_CLASS_VAL = re.compile(r"^\s*val\s+(\w+)\s*:", re.MULTILINE)
DATA_CLASS_PROP_TYPE = re.compile(r"^\s*val\s+\w+\s*:\s*([^=\n]+)", re.MULTILINE)


def extract_payload_keys(text: str, errors: list[str]) -> set[str]:
    headers: list[tuple[int, str, str, int]] = []
    for m in FUN_HEADER.finditer(text):
        receiver, name = m.group(1), m.group(2)
        if name.endswith("Payload") or name == "toPayload":
            headers.append((m.start(), receiver, name, m.end()))

    found = {(receiver, name) for _, receiver, name, _ in headers}
    for receiver, name in PAYLOAD_FUNCS:
        if (receiver, name) not in found:
            errors.append(
                f"payload function '{receiver}.{name}' is registered in PAYLOAD_FUNCS "
                "but not found in the payload source (renamed, given parameters, or moved?)"
            )

    keys: set[str] = set()
    for pos, receiver, name, header_end in headers:
        if (receiver, name) not in PAYLOAD_FUNCS:
            continue
        nxt = ANY_FUN.search(text, header_end)
        end = nxt.start() if nxt else len(text)
        keys.update(PAIR_KEY.findall(text[pos:end]))
    return keys


def extract_class_keys(text: str, cls: str) -> set[str]:
    m = re.search(rf"data class {cls}\s*\((.*?)\n\)", text, re.DOTALL)
    if not m:
        return set()
    return set(DATA_CLASS_VAL.findall(m.group(1)))


def extract_object_returned_keys(*texts: str) -> set[str]:
    keys: set[str] = set()
    for cls in OBJECT_RETURN_PAYLOAD_CLASSES:
        for text in texts:
            keys |= extract_class_keys(text, cls)
    return keys


def extract_shell_keys(text: str) -> set[str]:
    return set(SERIAL_NAME.findall(text))


def extract_shared_model_keys(shell_text: str, model_text: str) -> set[str]:
    keys: set[str] = set()
    queue = sorted(set(SHARED_TYPE_REF.findall(shell_text)))
    seen: set[str] = set()
    while queue:
        cls = queue.pop(0)
        if cls in seen:
            continue
        seen.add(cls)
        m = re.search(rf"data class {cls}\s*\((.*?)\n\)", model_text, re.DOTALL)
        if not m:
            continue
        body = m.group(1)
        keys.update(DATA_CLASS_VAL.findall(body))
        queue.extend(t for t in SHARED_TYPE_REF.findall(body) if t not in seen)
        for prop_type in DATA_CLASS_PROP_TYPE.findall(body):
            queue.extend(
                t
                for t in re.findall(r"\b([A-Z]\w*)", prop_type)
                if t not in seen and re.search(rf"data class {t}\s*\(", model_text)
            )
    return keys


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
    parser.add_argument("--model-file", type=Path, default=MODEL_FILE)
    parser.add_argument("--allowlist-file", type=Path, default=ALLOWLIST_FILE)
    args = parser.parse_args()

    errors: list[str] = []
    warnings: list[str] = []

    for f in (args.payload_file, args.apkconfig_file, args.shellconfig_file, args.model_file):
        if not f.is_file():
            errors.append(f"source file missing: {f}")

    if errors:
        for e in errors:
            print(f"ERROR: {e}")
        return 1

    payload_text = args.payload_file.read_text(encoding="utf-8")
    apkconfig_text = args.apkconfig_file.read_text(encoding="utf-8")
    shell_text = args.shellconfig_file.read_text(encoding="utf-8")
    model_text = args.model_file.read_text(encoding="utf-8")

    payload_keys = extract_payload_keys(payload_text, errors)
    payload_keys |= extract_object_returned_keys(apkconfig_text, shell_text)
    shell_keys = extract_shell_keys(shell_text)
    shared_model_keys = extract_shared_model_keys(shell_text, model_text)
    payload_keys |= shared_model_keys
    shell_keys |= shared_model_keys

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
