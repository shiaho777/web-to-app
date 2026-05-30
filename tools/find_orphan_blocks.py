#!/usr/bin/env python3
"""
Find orphan `when` blocks in Strings.kt.

A normal block looks like:
    val foo: String get() = when (lang) {
        AppLanguage.CHINESE -> "..."
        AppLanguage.ENGLISH -> "..."
        AppLanguage.ARABIC -> "..."
    }

An orphan block (caused by val-line being deleted) looks like:
        AppLanguage.ENGLISH -> "..."
        AppLanguage.ARABIC -> "..."
    }

We detect orphans by walking lines and finding `}` that closes a block where
the preceding line was an AppLanguage line but no `when (lang) {` opens it.
"""
import re
import sys
from pathlib import Path

PATH = Path(__file__).resolve().parent.parent / "app/src/main/java/com/webtoapp/core/i18n/Strings.kt"

def find_orphans(text: str):
    lines = text.splitlines()
    n = len(lines)
    orphans = []  # list of (start_line, end_line) ranges (0-indexed inclusive)
    i = 0
    while i < n:
        line = lines[i]
        # Walk lookahead for `        AppLanguage.X -> "..."`
        if re.match(r"^        AppLanguage\.(CHINESE|ENGLISH|ARABIC)\b", line):
            # search backwards for the most recent line that is either:
            #   - `val ... when (lang) {`  (means it's a real block)
            #   - `}`  with blank lines between (means it's an orphan)
            #   - another AppLanguage.X line (continue back)
            # Find the start of this `when` body
            j = i - 1
            while j >= 0:
                prev = lines[j]
                if re.match(r"^        AppLanguage\.(CHINESE|ENGLISH|ARABIC)\b", prev):
                    j -= 1
                    continue
                break
            # Now lines[j] is the line before this body block
            opens_block = j >= 0 and "when (lang) {" in lines[j]
            if not opens_block:
                # Orphan! Find its end (the next `}` line)
                k = i
                while k < n and not re.match(r"^\s*\}\s*$", lines[k]):
                    if re.match(r"^        AppLanguage\.", lines[k]):
                        k += 1
                    else:
                        # safety break
                        break
                # k points to the `}`
                start = j + 1  # first line of orphan body
                end = k
                orphans.append((start, end))
                i = k + 1
                continue
        i += 1
    return orphans

def main():
    text = PATH.read_text(encoding="utf-8")
    orphans = find_orphans(text)
    print(f"Total orphan blocks: {len(orphans)}")
    for s, e in orphans[:20]:
        print(f"--- lines {s+1}-{e+1} ---")
        for ln in range(s, min(e+1, len(text.splitlines()))):
            print(f"{ln+1:>6}: {text.splitlines()[ln]}")

if __name__ == "__main__":
    main()
