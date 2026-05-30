"""Find Strings val keys with no callers anywhere else in the codebase.

Usage: python3 tools/find_orphan_strings.py
Writes the list of orphan key names to tools/_orphan_strings.txt for
follow-up scripted removal. Read-only on its own.
"""

import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
STRINGS_PATH = os.path.join(ROOT, "app/src/main/java/com/webtoapp/core/i18n/Strings.kt")
SRC_ROOT = os.path.join(ROOT, "app/src/main/java")

def main() -> None:
    text = open(STRINGS_PATH).read()
    keys = sorted(set(re.findall(r"^    val (\w+):", text, flags=re.MULTILINE)))
    print(f"Total keys: {len(keys)}")

    files: list[str] = []
    for r, _, fs in os.walk(SRC_ROOT):
        for f in fs:
            if not f.endswith(".kt"):
                continue
            p = os.path.join(r, f)
            if p == STRINGS_PATH:
                continue
            files.append(p)

    sources: list[tuple[str, str]] = []
    for p in files:
        try:
            sources.append((p, open(p).read()))
        except OSError:
            pass

    orphans: list[str] = []
    for k in keys:
        pat = re.compile(rf"\bStrings\.{re.escape(k)}\b")
        if not any(pat.search(d) for _, d in sources):
            orphans.append(k)

    print(f"Orphan keys: {len(orphans)}")
    out_path = os.path.join(ROOT, "tools/_orphan_strings.txt")
    with open(out_path, "w") as f:
        for k in orphans:
            f.write(k + "\n")
    print(f"Wrote {out_path}")


if __name__ == "__main__":
    main()
