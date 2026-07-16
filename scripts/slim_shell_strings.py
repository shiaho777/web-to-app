#!/usr/bin/env python3
import re
import sys
from pathlib import Path

LANGS = [
    ("CHINESE", "zh"),
    ("ENGLISH", "en"),
    ("ARABIC", "ar"),
    ("PORTUGUESE", "pt"),
    ("SPANISH", "es"),
    ("FRENCH", "fr"),
    ("GERMAN", "de"),
    ("RUSSIAN", "ru"),
    ("JAPANESE", "ja"),
    ("KOREAN", "ko"),
]
LANG_NAMES = [x[0] for x in LANGS]


def skip_string(text: str, i: int) -> int:
    n = len(text)
    if i >= n or text[i] not in "\"'":
        return i
    if text[i] == '"' and i + 2 < n and text[i : i + 3] == '"""':
        i += 3
        while i + 2 < n:
            if text[i : i + 3] == '"""':
                return i + 3
            if text[i] == "\\":
                i += 2
                continue
            i += 1
        return n
    quote = text[i]
    i += 1
    while i < n:
        ch = text[i]
        if ch == "\\":
            i += 2
            continue
        if ch == quote:
            return i + 1
        i += 1
    return n


def skip_line_comment(text: str, i: int) -> int:
    n = len(text)
    i += 2
    while i < n and text[i] != "\n":
        i += 1
    return i


def skip_block_comment(text: str, i: int) -> int:
    n = len(text)
    i += 2
    while i + 1 < n:
        if text[i] == "*" and text[i + 1] == "/":
            return i + 2
        i += 1
    return n


def find_matching_brace(text: str, open_idx: int) -> int:
    if open_idx < 0 or open_idx >= len(text) or text[open_idx] != "{":
        return -1
    depth = 0
    i = open_idx
    n = len(text)
    while i < n:
        ch = text[i]
        if ch == '"' or ch == "'":
            i = skip_string(text, i)
            continue
        if ch == "/" and i + 1 < n:
            nxt = text[i + 1]
            if nxt == "/":
                i = skip_line_comment(text, i)
                continue
            if nxt == "*":
                i = skip_block_comment(text, i)
                continue
        if ch == "{":
            depth += 1
        elif ch == "}":
            depth -= 1
            if depth == 0:
                return i
        i += 1
    return -1


def is_ident_char(ch: str) -> bool:
    return ch.isalnum() or ch == "_"


def skip_ws_and_comments(text: str, i: int) -> int:
    n = len(text)
    while i < n:
        ch = text[i]
        if ch in " \t\r\n":
            i += 1
            continue
        if ch == "/" and i + 1 < n:
            if text[i + 1] == "/":
                i = skip_line_comment(text, i)
                continue
            if text[i + 1] == "*":
                i = skip_block_comment(text, i)
                continue
        break
    return i


def scan_expression_end(text: str, start: int) -> int:
    n = len(text)
    i = start
    depth_brace = 0
    depth_paren = 0
    depth_bracket = 0
    while i < n:
        ch = text[i]
        if ch == '"' or ch == "'":
            i = skip_string(text, i)
            continue
        if ch == "/" and i + 1 < n:
            nxt = text[i + 1]
            if nxt == "/":
                i = skip_line_comment(text, i)
                continue
            if nxt == "*":
                i = skip_block_comment(text, i)
                continue
        if ch == "{":
            depth_brace += 1
            i += 1
            continue
        if ch == "}":
            if depth_brace == 0 and depth_paren == 0 and depth_bracket == 0:
                return i
            depth_brace -= 1
            i += 1
            continue
        if ch == "(":
            depth_paren += 1
            i += 1
            continue
        if ch == ")":
            if depth_paren > 0:
                depth_paren -= 1
            i += 1
            continue
        if ch == "[":
            depth_bracket += 1
            i += 1
            continue
        if ch == "]":
            if depth_bracket > 0:
                depth_bracket -= 1
            i += 1
            continue
        if depth_brace == 0 and depth_paren == 0 and depth_bracket == 0:
            if text.startswith("AppLanguage.", i):
                return i
            if text.startswith("else", i):
                after = i + 4
                if after >= n or not is_ident_char(text[after]):
                    line_start = text.rfind("\n", start, i) + 1
                    if text[line_start:i].strip() == "":
                        return i
            if ch == "\n":
                j = i + 1
                while j < n and text[j] in " \t":
                    j += 1
                if j < n and (
                    text.startswith("AppLanguage.", j)
                    or (
                        text.startswith("else", j)
                        and (j + 4 >= n or not is_ident_char(text[j + 4]))
                    )
                ):
                    return i
        i += 1
    return n


def extract_arrow_value(body: str, lang: str):
    marker = f"AppLanguage.{lang}"
    p = 0
    n = len(body)
    while True:
        idx = body.find(marker, p)
        if idx < 0:
            return None
        after = idx + len(marker)
        if after < n and is_ident_char(body[after]):
            p = after
            continue
        before_ok = idx == 0 or not is_ident_char(body[idx - 1])
        if not before_ok:
            p = after
            continue
        i = skip_ws_and_comments(body, after)
        if i + 1 >= n or body[i : i + 2] != "->":
            p = after
            continue
        start = skip_ws_and_comments(body, i + 2)
        end = scan_expression_end(body, start)
        if end <= start:
            return None
        val = body[start:end].rstrip()
        if val.endswith(","):
            val = val[:-1].rstrip()
        return val


def brace_balance(text: str) -> int:
    depth = 0
    i = 0
    n = len(text)
    while i < n:
        ch = text[i]
        if ch == '"' or ch == "'":
            i = skip_string(text, i)
            continue
        if ch == "/" and i + 1 < n:
            nxt = text[i + 1]
            if nxt == "/":
                i = skip_line_comment(text, i)
                continue
            if nxt == "*":
                i = skip_block_comment(text, i)
                continue
        if ch == "{":
            depth += 1
        elif ch == "}":
            depth -= 1
        i += 1
    return depth


def decode_kotlin_string_literal(val: str):
    v = val.strip()
    if v.startswith('"""') and v.endswith('"""') and len(v) >= 6:
        return v[3:-3], True
    if len(v) >= 2 and v[0] == '"':
        end = skip_string(v, 0)
        if end != len(v):
            return None, False
        out = []
        i = 1
        n = len(v) - 1
        while i < n:
            ch = v[i]
            if ch == "\\":
                if i + 1 >= n:
                    return None, False
                nxt = v[i + 1]
                mapping = {
                    "\\": "\\",
                    '"': '"',
                    "'": "'",
                    "n": "\n",
                    "r": "\r",
                    "t": "\t",
                    "$": "$",
                    "b": "\b",
                }
                if nxt in mapping:
                    out.append(mapping[nxt])
                    i += 2
                    continue
                if nxt == "u" and i + 5 < n:
                    try:
                        out.append(chr(int(v[i + 2 : i + 6], 16)))
                        i += 6
                        continue
                    except Exception:
                        return None, False
                return None, False
            if ch == "$":
                return None, False
            out.append(ch)
            i += 1
        return "".join(out), True
    return None, False


def is_static_literal(val: str) -> bool:
    decoded, ok = decode_kotlin_string_literal(val)
    return ok and decoded is not None


def inject_table_hooks(text: str) -> str:
    if "ShellStringTable" not in text:
        text = text.replace(
            "import com.webtoapp.R\n",
            "import com.webtoapp.R\n",
            1,
        )
    # setLanguage
    old_set = """    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
    }"""
    new_set = """    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        val ctx = localizedContext ?: fallbackContext
        if (ctx != null) {
            ShellStringTable.load(ctx, language)
        }
    }"""
    if old_set in text:
        text = text.replace(old_set, new_set, 1)
    old_init = """    fun initialize(baseContext: Context) {
        val appContext = baseContext.applicationContext
        fallbackContext = appContext
        if (localizedContext == null) {
            localizedContext = appContext
        }
        contextVersion.intValue++
    }"""
    new_init = """    fun initialize(baseContext: Context) {
        val appContext = baseContext.applicationContext
        fallbackContext = appContext
        if (localizedContext == null) {
            localizedContext = appContext
        }
        ShellStringTable.load(appContext, _currentLanguage.value)
        contextVersion.intValue++
    }"""
    if old_init in text:
        text = text.replace(old_init, new_init, 1)
    old_attach = """    fun attachContext(baseContext: Context, language: AppLanguage = lang) {
        val appContext = baseContext.applicationContext
        fallbackContext = appContext
        localizedContext = try {
            LanguageManager.getInstance(baseContext).applyLanguage(baseContext, language)
        } catch (_: Exception) {
            appContext
        }
        contextVersion.intValue++
    }"""
    new_attach = """    fun attachContext(baseContext: Context, language: AppLanguage = lang) {
        val appContext = baseContext.applicationContext
        fallbackContext = appContext
        localizedContext = try {
            LanguageManager.getInstance(baseContext).applyLanguage(baseContext, language)
        } catch (_: Exception) {
            appContext
        }
        _currentLanguage.value = language
        ShellStringTable.load(appContext, language)
        contextVersion.intValue++
    }"""
    if old_attach in text:
        text = text.replace(old_attach, new_attach, 1)
    return text


def shell_string_table_source() -> str:
    return """package com.webtoapp.core.i18n

import android.content.Context
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.util.concurrent.atomic.AtomicReference
import java.util.zip.GZIPInputStream

object ShellStringTable {
    private val primary = AtomicReference<Array<String>>(emptyArray())
    private val fallback = AtomicReference<Array<String>>(emptyArray())
    @Volatile private var loadedCode: String? = null

    fun load(context: Context, language: AppLanguage) {
        val code = language.code
        if (loadedCode == code && primary.get().isNotEmpty()) {
            return
        }
        val app = context.applicationContext
        val selected = readPack(app, code)
        val english = if (code == "en") selected else readPack(app, "en")
        primary.set(if (selected.isNotEmpty()) selected else english)
        if (fallback.get().isEmpty()) {
            fallback.set(if (english.isNotEmpty()) english else primary.get())
        }
        loadedCode = code
    }

    fun get(id: Int): String {
        val p = primary.get()
        if (id >= 0 && id < p.size) {
            return p[id]
        }
        val f = fallback.get()
        if (id >= 0 && id < f.size) {
            return f[id]
        }
        return ""
    }

    private fun readPack(context: Context, code: String): Array<String> {
        val names = arrayOf("i18n/$code.pack", "shell_i18n/$code.pack")
        for (name in names) {
            try {
                context.assets.open(name).use { raw ->
                    return decodePack(raw)
                }
            } catch (_: Exception) {
            }
        }
        return emptyArray()
    }

    private fun decodePack(raw: java.io.InputStream): Array<String> {
        DataInputStream(BufferedInputStream(GZIPInputStream(raw))).use { input ->
            val b0 = input.readUnsignedByte()
            val b1 = input.readUnsignedByte()
            val b2 = input.readUnsignedByte()
            val b3 = input.readUnsignedByte()
            if (b0 != 'W'.code || b1 != 'T'.code || b2 != 'A'.code || b3 != '1'.code) {
                throw IllegalArgumentException("bad i18n magic")
            }
            val count = input.readInt()
            if (count < 0 || count > 200000) {
                throw IllegalArgumentException("bad i18n count")
            }
            return Array(count) {
                val len = input.readInt()
                if (len < 0 || len > 2_000_000) {
                    throw IllegalArgumentException("bad i18n string length")
                }
                val bytes = ByteArray(len)
                input.readFully(bytes)
                String(bytes, Charsets.UTF_8)
            }
        }
    }
}
"""




def extract_object_members(body: str) -> list:
    import re
    members = []
    i = 0
    n = len(body)
    while i < n:
        m = re.match(r"\s*(val|fun)\s+(\w+)", body[i:])
        if not m:
            i += 1
            continue
        kind = m.group(1)
        name = m.group(2)
        start = i + m.start(1)
        j = i + m.end()
        depth_p = 0
        eq = -1
        brace_body = -1
        sig_end = j
        k = j
        while k < n:
            ch = body[k]
            if ch == '"' or ch == "'":
                k = skip_string(body, k)
                continue
            if ch == "(":
                depth_p += 1
            elif ch == ")":
                depth_p -= 1
            elif ch == "{" and depth_p == 0:
                brace_body = k
                sig_end = k
                break
            elif ch == "=" and depth_p == 0:
                eq = k
                sig_end = k
                break
            k += 1
        signature = body[j:sig_end].strip()
        if brace_body >= 0:
            end = find_matching_brace(body, brace_body)
            members.append((kind, name, signature, body[start : end + 1].strip()))
            i = end + 1
            continue
        if eq < 0:
            i = j
            continue
        k = eq + 1
        depth = 0
        while k < n:
            ch = body[k]
            if ch == '"' or ch == "'":
                k = skip_string(body, k)
                continue
            if ch == "/" and k + 1 < n and body[k + 1] == "/":
                k = skip_line_comment(body, k)
                continue
            if ch == "{":
                depth += 1
            elif ch == "}":
                depth -= 1
            elif depth == 0 and ch == "\n":
                nxt = skip_ws_and_comments(body, k + 1)
                if nxt < n and (
                    body.startswith("val ", nxt) or body.startswith("fun ", nxt)
                ):
                    break
            k += 1
        members.append((kind, name, signature, body[start:k].strip()))
        i = k
    return members


def flatten_string_facade(text: str) -> str:
    import re
    impl = {}
    for letter in "ABCDE":
        marker = f"object Strings{letter}"
        start = text.find(marker)
        if start < 0:
            continue
        brace = text.find("{", start)
        end = find_matching_brace(text, brace)
        if end < 0:
            continue
        for kind, name, signature, body in extract_object_members(text[brace + 1 : end]):
            key = (kind, name, signature)
            impl[key] = body
            # also index by name+kind for simple lookup
            impl.setdefault((kind, name), body)
    if not impl:
        return text
    out = text
    for letter in "EDCBA":
        marker = f"object Strings{letter}"
        start = out.find(marker)
        if start < 0:
            continue
        brace = out.find("{", start)
        end = find_matching_brace(out, brace)
        if end < 0:
            continue
        end2 = end + 1
        while end2 < len(out) and out[end2] in "\r\n ":
            end2 += 1
        out = out[:start] + out[end2:]

    def indent_member(body: str) -> str:
        lines = body.split("\n")
        return "\n".join(("    " + line if line.strip() else line) for line in lines)

    def repl_val(match):
        name = match.group(3)
        body = impl.get(("val", name))
        if body is None:
            return match.group(0)
        return indent_member(body)

    def repl_fun(match):
        name = match.group(3)
        # prefer exact fun name; signature may differ slightly
        body = impl.get(("fun", name))
        if body is None:
            return match.group(0)
        return indent_member(body)

    out = re.sub(
        r"^[ \t]*val\s+(\w+)([^\n=]*?)\s*get\(\)\s*=\s*Strings[A-E]\.(\w+)[ \t]*$",
        repl_val,
        out,
        flags=re.M,
    )
    out = re.sub(
        r"^[ \t]*fun\s+(\w+)(\([^\)]*\)[^\n=]*?)\s*=\s*Strings[A-E]\.(\w+)\s*(\([^\n]*\))?[ \t]*$",
        repl_fun,
        out,
        flags=re.M,
    )
    out = re.sub(r"\n{3,}", "\n\n", out)
    return out


def transform(text: str):
    packs = {code: [] for _, code in LANGS}
    out = []
    i = 0
    key = "when (Strings.lang)"
    static_count = 0
    dynamic_count = 0
    while True:
        j = text.find(key, i)
        if j < 0:
            out.append(text[i:])
            break
        out.append(text[i:j])
        brace_start = text.find("{", j)
        if brace_start < 0:
            out.append(text[j:])
            break
        k = find_matching_brace(text, brace_start)
        if k < 0:
            out.append(text[j:])
            break
        when_body = text[brace_start + 1 : k]
        values = {}
        for lang_name, code in LANGS:
            values[code] = extract_arrow_value(when_body, lang_name)
        if all(v is None for v in values.values()):
            out.append(text[j : k + 1])
            i = k + 1
            continue
        for lang_name, code in LANGS:
            if values[code] is None:
                values[code] = values.get("en") or values.get("zh")
        static_ok = all(is_static_literal(values[code] or '""') for _, code in LANGS)
        if static_ok:
            sid = static_count
            for _, code in LANGS:
                decoded, _ = decode_kotlin_string_literal(values[code] or '""')
                packs[code].append(decoded if decoded is not None else "")
            out.append(f"ShellStringTable.get({sid})")
            static_count += 1
        else:
            branches = []
            for lang_name, code in LANGS:
                val = values[code]
                if val is None:
                    continue
                branches.append(f"        AppLanguage.{lang_name} -> {val}")
            if not branches:
                out.append(text[j : k + 1])
            else:
                out.append(
                    "when (Strings.lang) {\n"
                    + "\n".join(branches)
                    + "\n    }"
                )
            dynamic_count += 1
        i = k + 1
    result = "".join(out)
    result = inject_table_hooks(result)
    return result, packs, static_count, dynamic_count


def collect_used_string_props(repo_root: Path) -> set:
    import re
    used = set()
    core_pkgs = [
        "shell", "activation", "announcement", "adblock", "webview", "crypto",
        "i18n", "logging", "dns", "forcedrun", "floatingwindow", "privacy",
        "appearance", "actions", "perf", "port", "extension", "notification",
        "backgroundrun", "translate", "bgm", "engine", "script", "network",
        "errorpage", "golang", "python", "nodejs", "php", "wordpress", "sample",
        "autostart", "background", "linux", "download", "kernel", "frontend",
        "feature",
    ]
    roots = [
        repo_root / "app/src/main/java/com/webtoapp/ui/shell",
        repo_root / "app/src/main/java/com/webtoapp/ui/design",
        repo_root / "app/src/main/java/com/webtoapp/ui/theme",
        repo_root / "app/src/main/java/com/webtoapp/ui/shared",
        repo_root / "app/src/main/java/com/webtoapp/data/model",
        repo_root / "app/src/main/java/com/webtoapp/data/converter",
        repo_root / "shell/src/main/java",
        repo_root / "shell/src/main/java-overrides",
        repo_root / "shell/src/shellOnly",
    ]
    for pkg in core_pkgs:
        roots.append(repo_root / "app/src/main/java/com/webtoapp/core" / pkg)
    for name in [
        "announcement/AnnouncementTemplates.kt",
        "PremiumComponents.kt",
        "EdgeSwipeRefreshLayout.kt",
        "VirtualNavigationBar.kt",
        "StatusBarBackground.kt",
        "LongPressMenu.kt",
        "ForcedRunCountdownOverlay.kt",
        "AutoRefreshCountdownOverlay.kt",
    ]:
        roots.append(repo_root / "app/src/main/java/com/webtoapp/ui/components" / name)
    util_root = repo_root / "app/src/main/java/com/webtoapp/util"
    if util_root.is_dir():
        roots.append(util_root)
    pat = re.compile(r"Strings\.(\w+)")
    for root in roots:
        path = Path(root)
        if path.is_file():
            files = [path]
        elif path.is_dir():
            files = list(path.rglob("*.kt"))
        else:
            continue
        for f in files:
            try:
                body = f.read_text(encoding="utf-8")
            except Exception:
                continue
            used.update(pat.findall(body))
    return used


def zero_unused_static_strings(result: str, packs: dict, used_props: set):
    import re
    prop_to_id = {
        m.group(1): int(m.group(2))
        for m in re.finditer(
            r"val (\w+): String get\(\) = ShellStringTable\.get\((\d+)\)",
            result,
        )
    }
    keep_ids = {prop_to_id[p] for p in used_props if p in prop_to_id}
    cleared = 0
    for code, vals in packs.items():
        for i in range(len(vals)):
            if i not in keep_ids and vals[i]:
                vals[i] = ""
                cleared += 1
    return len(keep_ids), cleared // max(1, len(packs))



def main():
    if len(sys.argv) < 3:
        print(
            "usage: slim_shell_strings.py <Strings.kt> <outStrings.kt> [outDir]",
            file=sys.stderr,
        )
        sys.exit(2)
    src = Path(sys.argv[1])
    out_strings = Path(sys.argv[2])
    out_dir = Path(sys.argv[3]) if len(sys.argv) > 3 else out_strings.parent
    text = src.read_text(encoding="utf-8")
    result, packs, static_count, dynamic_count = transform(text)
    result = flatten_string_facade(result)
    repo_root = src.resolve().parents[5] if len(src.resolve().parents) > 5 else src.resolve().parent
    # Strings.kt path: app/src/main/java/com/webtoapp/core/i18n/Strings.kt -> parents[5]=app? 
    # app/src/main/java/com/webtoapp/core/i18n -> 0=i18n 1=core 2=webtoapp 3=java 4=main 5=src 6=app 7=root
    for parent in src.resolve().parents:
        if (parent / "app" / "src" / "main" / "java").is_dir() and (parent / "shell").is_dir():
            repo_root = parent
            break
    used_props = collect_used_string_props(repo_root)
    kept, cleared = zero_unused_static_strings(result, packs, used_props)
    bal = brace_balance(result)
    if bal != 0:
        print(f"error: brace balance = {bal}", file=sys.stderr)
        sys.exit(1)
    lengths = {code: len(vals) for code, vals in packs.items()}
    if len(set(lengths.values())) != 1:
        print(f"error: pack length mismatch {lengths}", file=sys.stderr)
        sys.exit(1)
    out_strings.parent.mkdir(parents=True, exist_ok=True)
    out_strings.write_text(result, encoding="utf-8")
    table_path = out_strings.parent / "ShellStringTable.kt"
    table_path.write_text(shell_string_table_source(), encoding="utf-8")

    slim_root = None
    for parent in out_strings.parents:
        if parent.name == "slim-i18n":
            slim_root = parent
            break
    if slim_root is None and out_strings.parent.name == "i18n":
        try:
            slim_root = out_strings.parents[4]
        except IndexError:
            slim_root = out_strings.parent
    if slim_root is None:
        slim_root = out_strings.parent
    assets_dir = slim_root / "assets" / "i18n"
    assets_dir.mkdir(parents=True, exist_ok=True)

    import gzip
    import struct

    def encode_pack(strings: list) -> bytes:
        parts = [b"WTA1", struct.pack(">I", len(strings))]
        for s in strings:
            b = s.encode("utf-8")
            parts.append(struct.pack(">I", len(b)))
            parts.append(b)
        return gzip.compress(b"".join(parts), compresslevel=9)

    for _, code in LANGS:
        pack_path = assets_dir / f"{code}.pack"
        pack_path.write_bytes(encode_pack(packs[code]))
    for old in assets_dir.glob("*.json"):
        old.unlink()
    total_asset = sum((assets_dir / f"{code}.pack").stat().st_size for _, code in LANGS)
    print(
        f"in_bytes={len(text)} out_bytes={len(result)} "
        f"static={static_count} dynamic={dynamic_count} "
        f"langs={len(LANGS)} asset_bytes={total_asset} brace_balance={bal} kept_ids={kept} cleared_per_lang={cleared} used_props={len(used_props)}"
    )



if __name__ == "__main__":
    main()
