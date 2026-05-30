"""Remove a list of orphan Strings val blocks from Strings.kt.

Reads the keys-to-remove from a hard-coded list (we keep this explicit so
nothing surprising gets deleted) and rewrites Strings.kt with each
matching `val <key>: ...` block stripped, including the `}` that closes
its `when (lang) { ... }` body and the trailing blank line.
"""

import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
STRINGS_PATH = os.path.join(ROOT, "app/src/main/java/com/webtoapp/core/i18n/Strings.kt")

# Orphan AI-coding / HTML-coding / module-developer keys that have no callers
# anywhere outside Strings.kt itself.
KEYS_TO_REMOVE = [
    # legacy AI coding session UI
    "aiCodingAssistant",
    "aiCodingFileAlreadyExists",
    "aiCodingNoOutputFallback",
    "aiCodingToolCallEmpty",
    "aiCodingWelcome",

    # legacy AI generation foreground service
    "aiGenerationService",
    "aiGenerationServiceRunning",
    "aiHtmlCodingFeature",

    # AI module developer screen
    "aiModuleCopiedToClipboard",
    "aiModuleDeveloper",
    "aiModuleDeveloperAgent",
    "aiModuleDeveloperTitle",
    "aiModuleDevelopment",
    "aiModuleFeatureRequired",
    "aiModuleGenerateSuccess",
    "aiModuleNoCodeToValidate",
    "aiModuleNoOtherModels",
    "aiModuleSaveFailed",
    "aiModuleSaved",
    "aiModuleSessionDiscarded",
    "aiModuleSessionRestoreFailed",
    "aiModuleSessionRestored",
    "aiModuleSwitchedToModel",

    # legacy v1 HTML tool palette
    "autoFixDesc",
    "checkSyntaxDesc",
    "editHtmlDesc",
    "featureAutoFix",
    "featureCheckSyntax",
    "featureEditHtml",
    "featureGetConsoleLogs",
    "featureReadCurrentCode",
    "featureWriteHtml",
    "generateImageDesc",
    "getConsoleLogsDesc",
    "readCurrentCodeDesc",
    "writeHtmlDesc",
    "toolAutoFix",
    "toolAutoFixDesc",
    "toolCheckSyntax",
    "toolCheckSyntaxDesc",
    "toolEditHtml",
    "toolEditHtmlDesc",
    "toolGenerateImage",
    "toolGenerateImageDesc",
    "toolGetConsoleLogs",
    "toolGetConsoleLogsDesc",
    "toolReadCurrentCode",
    "toolReadCurrentCodeDesc",
    "toolWriteHtml",
    "toolWriteHtmlDesc",

    # legacy code generation status messages
    "codeGenerationComplete",
    "fileCreatedVersion",
    "generatingCode",
    "generatingCodeChars",
    "generatingCodeStatus",
    "generationCancelled",
    "generationFailed",
    "suggestionChangeModel",
    "newFile",

    # legacy v1 syntax checker (fully removed when we deleted AiCodingAgent)
    "extraClosingBrace",
    "extraClosingBraces",
    "extraClosingBrackets",
    "extraClosingParens",
    "missingClosingBraces",
    "missingClosingBrackets",
    "missingClosingParens",
    "tagNotClosed",
    "tagNotProperlyClosed",
    "unexpectedClosingTag",

    # legacy "image generation" inline label (replaced by AI_CODING_IMAGE feature label)
    "aiImageGeneration",

    # legacy "use Chinese" hard-coded rule injection
    "ruleUseChinese",
]


def remove_block(text: str, key: str) -> tuple[str, bool]:
    """Remove `    val <key>: String get() = ... }` and any trailing blank line.

    The block ends at the matching `}` for the `get() =` body. Strings.kt
    blocks come in two shapes:

      1. A `when` expression that spans multiple lines and ends with `}`.
      2. A single-line `else -> getString(...)` that still uses `}`.

    We track brace depth from the line containing `val <key>:` and stop at
    the line where depth returns to zero.
    """
    lines = text.split("\n")
    start = None
    for i, line in enumerate(lines):
        if re.match(rf"^    val {re.escape(key)}: ", line):
            start = i
            break
    if start is None:
        return text, False

    # Walk forward, counting `{` and `}` in non-string content. The first
    # `{` is on the start line itself.
    depth = 0
    end = None
    for j in range(start, len(lines)):
        for ch in lines[j]:
            if ch == "{":
                depth += 1
            elif ch == "}":
                depth -= 1
                if depth == 0:
                    end = j
                    break
        if end is not None:
            break
    if end is None:
        return text, False

    # Drop trailing blank line(s) that were separating this block.
    drop_to = end + 1
    while drop_to < len(lines) and lines[drop_to].strip() == "":
        drop_to += 1
        # Only consume up to one blank line — keep the structure tidy.
        break

    new_lines = lines[:start] + lines[drop_to:]
    return "\n".join(new_lines), True


def main() -> int:
    text = open(STRINGS_PATH).read()
    removed: list[str] = []
    skipped: list[str] = []
    for key in KEYS_TO_REMOVE:
        text, ok = remove_block(text, key)
        (removed if ok else skipped).append(key)
    open(STRINGS_PATH, "w").write(text)
    print(f"Removed {len(removed)} blocks, skipped {len(skipped)}.")
    if skipped:
        print("Skipped (not found): " + ", ".join(skipped))
    return 0


if __name__ == "__main__":
    sys.exit(main())
