#!/usr/bin/env python3
"""
Validate the WebToApp Module Market catalog at `modules/`.

This is the source of truth for what `modules/README.md` claims gets
checked at CI time. Every rule here corresponds to a real failure mode
that would either reject a module at install time or quietly waste space
on disk.

The validator is intentionally written with only Python's standard
library — no pip install step, no extra container — so the CI job stays
fast and deterministic.

Run locally:

    python3 .github/scripts/ci/validate_modules.py

Exit codes:
    0 = catalog is valid
    1 = at least one error was found (CI fails)
"""

from __future__ import annotations

import json
import os
import re
import sys
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any, Iterable


# ───────────────────────── enum values (must mirror the Kotlin source) ─

# `ModuleCategory` enum in `core/extension/ExtensionModule.kt`.
ALLOWED_CATEGORIES: set[str] = {
    "CONTENT_FILTER", "CONTENT_ENHANCE", "STYLE_MODIFIER", "THEME",
    "FUNCTION_ENHANCE", "AUTOMATION", "NAVIGATION", "DATA_EXTRACT",
    "DATA_SAVE", "INTERACTION", "ACCESSIBILITY", "MEDIA", "VIDEO",
    "IMAGE", "AUDIO", "SECURITY", "ANTI_TRACKING", "SOCIAL", "SHOPPING",
    "READING", "TRANSLATE", "DEVELOPER", "OTHER",
}

# `ModuleRunTime` enum.
ALLOWED_RUN_AT: set[str] = {
    "DOCUMENT_START", "DOCUMENT_END", "DOCUMENT_IDLE",
    "CONTEXT_MENU", "BEFORE_UNLOAD",
}

# `ModulePermission` enum.
ALLOWED_PERMISSIONS: set[str] = {
    "DOM_ACCESS", "DOM_OBSERVE", "CSS_INJECT", "STORAGE", "COOKIE",
    "INDEXED_DB", "CACHE", "NETWORK", "WEBSOCKET", "FETCH_INTERCEPT",
    "CLIPBOARD", "NOTIFICATION", "ALERT", "KEYBOARD", "MOUSE", "TOUCH",
    "LOCATION", "CAMERA", "MICROPHONE", "DEVICE_INFO", "MEDIA",
    "FULLSCREEN", "PICTURE_IN_PICTURE", "SCREEN_CAPTURE", "DOWNLOAD",
    "FILE_ACCESS", "EVAL", "IFRAME", "WINDOW_OPEN", "HISTORY",
    "NAVIGATION",
}

ALLOWED_SOURCE_TYPES: set[str] = {"CUSTOM", "CHROME_EXTENSION"}
STORE_ID_RE = re.compile(r"^[a-p]{32}$")


# ───────────────────────── regex helpers ───────────────────────────────

KEBAB_CASE_RE = re.compile(r"^[a-z0-9]+(?:-[a-z0-9]+)*$")
SEMVER_RE = re.compile(r"^\d+(?:\.\d+){0,3}(?:[-+][\w.-]+)?$")

# Files we let people drop in besides the ones the runtime actually
# downloads (plugin.json, plugin.html; legacy main.js/style.css/panel.html).
ALLOWED_EXTRA_FILES: set[str] = {
    "README.md",       # nice for module pages on GitHub
    "CHANGELOG.md",
    "LICENSE",
    "LICENSE.md",
    ".gitkeep",
}

# Image files contributors may drop next to `plugin.html` for use as the
# module icon. The CI generator does not download these — they are served
# via the same GitHub raw / jsDelivr fallback the runtime already uses.
ALLOWED_ICON_FILES: set[str] = {
    "icon.png", "icon.svg", "icon.webp", "icon.jpg", "icon.jpeg",
}

# Maximum size for an inlined icon. Larger icons should be hosted off-repo
# and referenced via an absolute `iconUrl`. 256 KB is a forgiving limit:
# a 256x256 PNG with reasonable compression sits comfortably below it.
MAX_ICON_BYTES = 256 * 1024


@dataclass
class Report:
    """Accumulates diagnostics and pretty-prints them at the end."""

    errors: list[str] = field(default_factory=list)
    warnings: list[str] = field(default_factory=list)

    def error(self, where: str, message: str) -> None:
        self.errors.append(f"  ❌ {where}: {message}")

    def warning(self, where: str, message: str) -> None:
        self.warnings.append(f"  ⚠️  {where}: {message}")

    def ok(self) -> bool:
        return not self.errors

    def render(self) -> str:
        lines: list[str] = []
        if self.errors:
            lines.append(f"\n{len(self.errors)} error(s):")
            lines.extend(self.errors)
        if self.warnings:
            lines.append(f"\n{len(self.warnings)} warning(s):")
            lines.extend(self.warnings)
        if not lines:
            lines.append("\nAll module checks passed.")
        return "\n".join(lines)


# ───────────────────────── primitive checks ────────────────────────────

def _is_str(value: Any) -> bool:
    return isinstance(value, str)


def _expect_str(report: Report, where: str, value: Any, field: str) -> str | None:
    if value is None:
        return None
    if not _is_str(value):
        report.error(where, f"`{field}` must be a string, got {type(value).__name__}")
        return None
    return value


def _expect_list_of_str(report: Report, where: str, value: Any, field: str) -> list[str]:
    if value is None:
        return []
    if not isinstance(value, list) or any(not _is_str(v) for v in value):
        report.error(where, f"`{field}` must be a list of strings")
        return []
    return value


def _validate_url_matches(
    report: Report, where: str, url_matches: Any
) -> None:
    if url_matches is None:
        return
    if not isinstance(url_matches, list):
        report.error(where, "`urlMatches` must be a list")
        return
    for i, rule in enumerate(url_matches):
        rule_loc = f"{where}::urlMatches[{i}]"
        if not isinstance(rule, dict):
            report.error(rule_loc, "must be an object with at least `pattern`")
            continue
        pattern = rule.get("pattern")
        if not _is_str(pattern) or not pattern:
            report.error(rule_loc, "`pattern` is required and must be a non-empty string")
        for flag in ("isRegex", "exclude"):
            if flag in rule and not isinstance(rule[flag], bool):
                report.error(rule_loc, f"`{flag}` must be a boolean")


def _validate_author(report: Report, where: str, author: Any) -> None:
    if author is None:
        return
    if not isinstance(author, dict):
        report.error(where, "`author` must be an object")
        return
    name = author.get("name")
    if not _is_str(name) or not name.strip():
        report.error(where, "`author.name` is required")
    for opt_key in ("email", "url", "qq"):
        if opt_key in author and author[opt_key] is not None and not _is_str(author[opt_key]):
            report.error(where, f"`author.{opt_key}` must be a string")


# ───────────────────────── registry checks ─────────────────────────────

def _validate_registry_entry(
    report: Report, entry: dict[str, Any], index: int
) -> None:
    where = f"registry.json::modules[{index}]"

    for required in ("id", "path", "name"):
        if not _is_str(entry.get(required)) or not entry[required].strip():
            report.error(where, f"`{required}` is required and must be a non-empty string")

    path = entry.get("path", "")
    if _is_str(path) and not KEBAB_CASE_RE.match(path):
        report.error(where, f"`path` must be kebab-case, got {path!r}")

    version = entry.get("version")
    if not _is_str(version) or not SEMVER_RE.match(version or ""):
        report.error(where, f"`version` must be a semver string, got {version!r}")

    category = entry.get("category", "OTHER")
    if not _is_str(category) or category not in ALLOWED_CATEGORIES:
        report.error(where, f"`category` must be one of the allowed values, got {category!r}")

    run_at = entry.get("runAt", "DOCUMENT_END")
    if not _is_str(run_at) or run_at not in ALLOWED_RUN_AT:
        report.error(where, f"`runAt` must be one of the allowed values, got {run_at!r}")

    for perm in _expect_list_of_str(report, where, entry.get("permissions"), "permissions"):
        if perm not in ALLOWED_PERMISSIONS:
            report.error(where, f"unknown permission {perm!r}")

    _validate_url_matches(report, where, entry.get("urlMatches"))
    _validate_author(report, where, entry.get("author"))

    if "minAppVersion" in entry and not isinstance(entry["minAppVersion"], int):
        report.error(where, "`minAppVersion` must be an integer")

    if "hasCss" in entry and not isinstance(entry["hasCss"], bool):
        report.error(where, "`hasCss` must be a boolean")

    icon_url = entry.get("iconUrl")
    if icon_url is not None and not _is_str(icon_url):
        report.error(where, "`iconUrl` must be a string when present")

    source_type = entry.get("sourceType", "CUSTOM")
    if not _is_str(source_type) or source_type not in ALLOWED_SOURCE_TYPES:
        report.error(where, f"`sourceType` must be one of {sorted(ALLOWED_SOURCE_TYPES)}, got {source_type!r}")

    if source_type == "CHROME_EXTENSION":
        store_id = entry.get("storeId")
        if not _is_str(store_id) or not STORE_ID_RE.match(store_id or ""):
            report.error(where, "`storeId` is required for CHROME_EXTENSION and must be a 32-char Chrome Web Store ID")


def _validate_registry(report: Report, registry: dict[str, Any]) -> list[dict[str, Any]]:
    where = "registry.json"
    schema = registry.get("schema")
    if schema != 1:
        report.error(where, f"`schema` must be 1 (got {schema!r}); future versions need a parser update")
    if "updatedAt" in registry and not _is_str(registry["updatedAt"]):
        report.error(where, "`updatedAt` must be a string")

    modules = registry.get("modules")
    if not isinstance(modules, list):
        report.error(where, "`modules` must be a list")
        return []

    seen_ids: set[str] = set()
    seen_paths: set[str] = set()
    for i, entry in enumerate(modules):
        if not isinstance(entry, dict):
            report.error(f"{where}::modules[{i}]", "must be an object")
            continue
        _validate_registry_entry(report, entry, i)

        eid = entry.get("id") if _is_str(entry.get("id")) else None
        if eid:
            if eid in seen_ids:
                report.error(f"{where}::modules[{i}]", f"duplicate id {eid!r}")
            if not KEBAB_CASE_RE.match(eid):
                report.error(f"{where}::modules[{i}]", f"id {eid!r} must be kebab-case")
            seen_ids.add(eid)

        epath = entry.get("path") if _is_str(entry.get("path")) else None
        if epath:
            if epath in seen_paths:
                report.error(f"{where}::modules[{i}]", f"duplicate path {epath!r}")
            seen_paths.add(epath)

    return [m for m in modules if isinstance(m, dict)]


# ───────────────────────── cross-file consistency ─────────────────────

def _validate_plugin_registry_consistency(
    report: Report,
    entry: dict[str, Any],
    plugin: dict[str, Any],
    folder: Path,
) -> None:
    """`plugin.json` and `registry.json` must agree on the shared fields."""

    where = f"modules/{folder.name}"

    if entry.get("id") != plugin.get("id"):
        report.error(
            where,
            f"`id` mismatch: registry says {entry.get('id')!r}, plugin.json says {plugin.get('id')!r}",
        )

    if entry.get("name") != plugin.get("name"):
        report.error(
            where,
            f"`name` mismatch: registry says {entry.get('name')!r}, plugin.json says {plugin.get('name')!r}",
        )

    reg_version = entry.get("version")
    if _is_str(reg_version) and plugin.get("version") != reg_version:
        report.error(
            where,
            f"`version` mismatch: registry={reg_version!r}, plugin.json={plugin.get('version')!r}",
        )


# ───────────────────────── per-folder structural checks ───────────────

def _validate_folder_layout(report: Report, folder: Path) -> None:
    """Each module folder must look like the schema says it does."""

    where = f"modules/{folder.name}"

    if not KEBAB_CASE_RE.match(folder.name):
        report.error(where, f"folder name must be kebab-case, got {folder.name!r}")

    if not (folder / "plugin.json").is_file():
        report.error(where, "missing required `plugin.json`")

    if not (folder / "plugin.html").is_file():
        if (folder / "main.js").is_file():
            report.warning(
                where,
                "legacy multi-file layout (main.js/panel.html) — prefer a single `plugin.html` with a `<script type=\"hcj/page\">` block",
            )
        else:
            report.error(where, "missing required `plugin.html`")

    if (folder / "module.json").is_file():
        report.error(
            where,
            "`module.json` is the retired extension-module manifest — remove it; the catalog is plugin.json-only",
        )

    # Flag stray files. The runtime ignores them, so they only bloat the repo.
    expected = {"plugin.json", "plugin.html", "main.js", "style.css", "panel.html"}
    for child in folder.iterdir():
        if child.name in expected or child.name in ALLOWED_EXTRA_FILES:
            continue
        if child.name in ALLOWED_ICON_FILES:
            # Icons are size-checked separately further below.
            continue
        if child.is_dir():
            report.warning(
                where,
                f"unexpected sub-directory `{child.name}/` — the runtime won't read it",
            )
        else:
            report.warning(
                where,
                f"unexpected file `{child.name}` — the runtime won't download it",
            )


def _validate_icon_coherence(
    report: Report, folder: Path, entry: dict[str, Any] | None
) -> None:
    """
    Cross-check the contributor-supplied icon against the registry.

    The contract:

    - If `iconUrl` is missing/empty, the app falls back to the first letter
      of the module name. No icon file is needed.
    - If `iconUrl` looks like a relative path, the corresponding file must
      actually exist next to `main.js` and stay within the size budget.
    - If `iconUrl` is absolute (starts with `http://` or `https://`), we
      defer to the contributor's hosting and do not check the file system.
    - The file name must come from `ALLOWED_ICON_FILES` so we don't end up
      shipping random binaries through the catalog.
    """
    where = f"modules/{folder.name}"
    icon_url = (entry or {}).get("iconUrl")

    icon_files_present = sorted(
        f for f in ALLOWED_ICON_FILES if (folder / f).is_file()
    )

    # Warn about extra random icon files when the registry doesn't reference
    # them — the runtime won't fetch a `logo.png` it has no way to know about.
    if not icon_url and icon_files_present:
        report.warning(
            where,
            f"icon file(s) {icon_files_present} present but registry has no "
            "`iconUrl` — they won't be used by the runtime",
        )

    if not _is_str(icon_url) or not icon_url.strip():
        return

    icon_url = icon_url.strip()
    if icon_url.startswith("http://") or icon_url.startswith("https://"):
        if icon_url.startswith("http://"):
            report.warning(where, "`iconUrl` uses http://; prefer https for catalog assets")
        return

    # Relative path case. Reject path traversal and force the file to live
    # inside the module folder with a known extension.
    normalised = icon_url.lstrip("./")
    if normalised.startswith("/") or ".." in normalised.split("/"):
        report.error(where, f"`iconUrl` must be a path inside the module folder, got {icon_url!r}")
        return
    if normalised not in ALLOWED_ICON_FILES:
        report.error(
            where,
            f"`iconUrl` must reference one of {sorted(ALLOWED_ICON_FILES)}, got {icon_url!r}",
        )
        return

    icon_path = folder / normalised
    if not icon_path.is_file():
        report.error(where, f"`iconUrl` points at {normalised!r} but no such file exists")
        return

    size = icon_path.stat().st_size
    if size > MAX_ICON_BYTES:
        report.error(
            where,
            f"`{normalised}` is {size // 1024} KB, over the {MAX_ICON_BYTES // 1024} KB cap; "
            "trim the icon or host it off-repo via an absolute URL",
        )


def _page_script_sources(report: Report, folder: Path) -> list[tuple[str, str]]:
    """Page-side JS sources: the hcj/page block(s) in plugin.html, plus any
    legacy main.js. Returns (where, content) pairs."""
    out: list[tuple[str, str]] = []
    plugin_html = folder / "plugin.html"
    if plugin_html.is_file():
        try:
            html = plugin_html.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            report.error(f"modules/{folder.name}/plugin.html", "must be UTF-8")
            html = ""
        blocks = re.findall(
            r'<script[^>]*type\s*=\s*["\']hcj/page["\'][^>]*>(.*?)</script>',
            html, flags=re.DOTALL | re.IGNORECASE,
        )
        if plugin_html.is_file() and not blocks and "main.js" not in html:
            report.warning(
                f"modules/{folder.name}/plugin.html",
                "has no `<script type=\"hcj/page\">` block — page-side code will not run",
            )
        if blocks:
            out.append((f"modules/{folder.name}/plugin.html", "\n".join(blocks)))
    main_js = folder / "main.js"
    if main_js.is_file():
        try:
            out.append((f"modules/{folder.name}/main.js", main_js.read_text(encoding="utf-8")))
        except UnicodeDecodeError:
            report.error(f"modules/{folder.name}/main.js", "must be UTF-8")
    return out


def _validate_main_js(report: Report, folder: Path) -> None:
    """Cheap heuristics on the page-side script (hcj/page block / main.js)."""

    sources = _page_script_sources(report, folder)
    if not sources:
        return

    for where, content in sources:
        _check_page_js(report, folder, where, content)


def _check_page_js(report: Report, folder: Path, where: str, content: str) -> None:
    if not content.strip():
        report.error(where, "is empty")
        return

    if len(content) > 512 * 1024:
        report.warning(where, f"is large ({len(content) // 1024} KB) — consider trimming")

    # No top-level `return` — the IIFE wrapper turns this into a syntax error.
    for lineno, line in enumerate(content.splitlines(), start=1):
        stripped = line.strip()
        if stripped.startswith("return ") or stripped == "return;" or stripped == "return":
            # Could still be inside a function literal at line 1; we can't
            # parse JS exactly, but this is a strong smell.
            indent = len(line) - len(line.lstrip())
            if indent == 0:
                report.error(
                    where,
                    f"line {lineno}: top-level `return` — the IIFE wrapper would make this a syntax error",
                )
                break

    # Legacy globals (`getConfig`/`__MODULE_*`) only work when the plugin
    # manifest opts into `legacyCompat` — flag code that assumes them
    # unconditionally.
    if ("getConfig(" in content or "__MODULE_" in content):
        try:
            plugin = json.loads((folder / "plugin.json").read_text(encoding="utf-8"))
            if plugin.get("legacyCompat") is not True:
                report.warning(
                    where,
                    "uses legacy `getConfig`/`__MODULE_*` globals but `plugin.json` does not set `legacyCompat: true`",
                )
        except (json.JSONDecodeError, OSError):
            pass


def _validate_plugin_json(
    report: Report,
    folder: Path,
    plugin: dict[str, Any],
) -> None:
    """Validate a per-module `plugin.json` (the HCJ package manifest)."""
    where = f"modules/{folder.name}/plugin.json"

    for required in ("id", "name"):
        if not _is_str(plugin.get(required)) or not str(plugin[required]).strip():
            report.error(where, f"`{required}` is required and must be a non-empty string")

    version = plugin.get("version")
    if not _is_str(version) or not SEMVER_RE.match(version or ""):
        report.error(where, f"`version` must be a semver string, got {version!r}")

    matches = plugin.get("matches")
    if matches is not None:
        if not isinstance(matches, list) or not all(_is_str(m) for m in matches):
            report.error(where, "`matches` must be a list of match-pattern strings")

    run_at = plugin.get("runAt")
    if run_at is not None and run_at not in ("document_start", "document_end", "document_idle"):
        report.error(where, f"`runAt` must be document_start/document_end/document_idle, got {run_at!r}")

    perms = plugin.get("permissions")
    if perms is not None:
        if not isinstance(perms, list) or not all(_is_str(p) for p in perms):
            report.error(where, "`permissions` must be a list of strings")
        else:
            allowed = {"STORAGE", "FETCH", "NOTIFY", "BADGE", "CLIPBOARD", "DOWNLOAD"}
            for p in perms:
                if p not in allowed:
                    report.warning(where, f"unknown permission {p!r} (allowed: {sorted(allowed)})")


# ───────────────────────── entry point ─────────────────────────────────

def _load_json(report: Report, path: Path, where: str) -> dict[str, Any] | None:
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except FileNotFoundError:
        report.error(where, f"file not found: {path}")
    except json.JSONDecodeError as e:
        report.error(where, f"invalid JSON: {e.msg} (line {e.lineno}, col {e.colno})")
    except UnicodeDecodeError:
        report.error(where, "file must be UTF-8")
    return None


def main(repo_root: Path) -> int:
    modules_dir = repo_root / "modules"
    if not modules_dir.is_dir():
        print(f"❌ {modules_dir} does not exist", file=sys.stderr)
        return 1

    report = Report()

    # 1. Parse + validate the registry.
    registry = _load_json(report, modules_dir / "registry.json", "registry.json")
    registry_entries: list[dict[str, Any]] = []
    if isinstance(registry, dict):
        registry_entries = _validate_registry(report, registry)

    # 2. Walk module folders.
    folders = sorted(
        p for p in modules_dir.iterdir()
        if p.is_dir() and not p.name.startswith(".")
    )

    folder_paths = {p.name for p in folders}
    registry_paths = {e["path"] for e in registry_entries if _is_str(e.get("path"))}
    custom_paths = {e["path"] for e in registry_entries if _is_str(e.get("path")) and e.get("sourceType", "CUSTOM") != "CHROME_EXTENSION"}

    # Folders missing a registry entry.
    for orphan in folder_paths - registry_paths:
        report.error(f"modules/{orphan}", "module folder has no entry in registry.json")
    # Registry entries pointing at non-existent folders (CHROME_EXTENSION entries don't need folders).
    for ghost in custom_paths - folder_paths:
        report.error("registry.json", f"entry refers to missing folder modules/{ghost}/")

    # 3. Per-folder validation + registry consistency.
    entries_by_path = {e["path"]: e for e in registry_entries if _is_str(e.get("path")) and e.get("sourceType", "CUSTOM") != "CHROME_EXTENSION"}
    for folder in folders:
        _validate_folder_layout(report, folder)
        _validate_main_js(report, folder)
        _validate_icon_coherence(report, folder, entries_by_path.get(folder.name))

        plugin_path = folder / "plugin.json"
        plugin_manifest = _load_json(report, plugin_path, f"modules/{folder.name}/plugin.json")
        if isinstance(plugin_manifest, dict):
            _validate_plugin_json(report, folder, plugin_manifest)
            entry = entries_by_path.get(folder.name)
            if entry:
                _validate_plugin_registry_consistency(report, entry, plugin_manifest, folder)

    print(report.render())
    return 0 if report.ok() else 1


if __name__ == "__main__":
    repo = Path(__file__).resolve().parents[3]
    sys.exit(main(repo))
