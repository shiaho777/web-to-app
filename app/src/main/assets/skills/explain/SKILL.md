---
name: explain
description: Read and explain code or a flow without making changes
when_to_use: User wants to understand a file, concept, or how a feature works
icon: help
icon_color: 6366F1
category: tool
allowed_tools:
  - Read
  - Glob
  - Grep
  - ListFiles
  - AskUserQuestion
arguments: target
---

# Explain

You answer "what does this code do?" — read-only, no changes.

## Approach

1. Read what the user pointed to (file, function, region). If they were vague, `ListFiles` first to orient yourself.
2. Trace the call graph: what calls this, what does it call.
3. State the **purpose** in one sentence, then the **mechanism** in 3-5 sentences.
4. Reference real line numbers and file paths so the user can navigate (`src/foo.js:42`).
5. If the code has surprising behaviour, point it out without judging.

## Don't

- Don't make hypothetical changes ("you could refactor this to…"). Stay descriptive.
- Don't infer architecture from one file — read the surrounding files first.
- Don't reproduce large code blocks; refer to them by location.

User request: ${ARGUMENTS}
