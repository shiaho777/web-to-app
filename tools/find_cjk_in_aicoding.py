"""Find any CJK characters left in the new aicoding code tree.

The new AI Coding subsystem is supposed to read all UI text from
Strings.kt rather than hardcode strings. This finder reports any line
under core/aicoding or ui/aicoding that still contains CJK literals.
"""

import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TARGETS = [
    os.path.join(ROOT, "app/src/main/java/com/webtoapp/core/aicoding"),
    os.path.join(ROOT, "app/src/main/java/com/webtoapp/ui/aicoding"),
]
CJK = re.compile(r"[\u4e00-\u9fff]")

def main() -> int:
    hits = 0
    for tree in TARGETS:
        for r, _, fs in os.walk(tree):
            for f in fs:
                if not f.endswith(".kt"):
                    continue
                p = os.path.join(r, f)
                with open(p, encoding="utf-8") as fh:
                    for i, line in enumerate(fh, 1):
                        if CJK.search(line):
                            print(f"{p}:{i}: {line.rstrip()}")
                            hits += 1
    print(f"Hits: {hits}")
    return 0 if hits == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
