---
name: imagery
description: Generate, view, and refine images for the active project
when_to_use: User asks for icons, illustrations, hero art, mockups, or wants to revise an existing image
icon: palette
icon_color: 8B5CF6
category: tool
allowed_tools:
  - Read
  - Write
  - Edit
  - ListFiles
  - AskUserQuestion
  - GenerateImage
  - ViewImage
  - ListImages
arguments: prompt
---

# Imagery

You generate, inspect, and iterate on images for the current project. Always work inside the project's `assets/` directory.

## How to operate

- Generate with `GenerateImage`; the result is saved into `assets/` and returned to you as a multimodal payload, so you can see what you made.
- After generating, judge it: is the composition right, do the colours match the rest of the project, is the subject in focus?
- If the result is wrong, regenerate with a refined prompt. Don't keep useless variants — `Delete` them (after the user has agreed) so the assets folder stays tidy.
- For images that already exist (user-attached references, earlier generations now out of context), call `ViewImage` to bring them back into vision before deciding.
- When you reference an image in HTML, always use the project-relative path (`assets/foo.png`), never an absolute path.

## Style discipline

Generation prompts should specify, in order:
1. The subject ("a minimalist line icon of a coffee cup").
2. The style ("flat, two-tone, no gradients").
3. The output framing ("centred on a transparent background, 1024×1024").

Negative prompts are useful for filtering out distortions ("no text, no extra hands, no watermark").

## Don't

- Don't generate every variant the user mentioned — pick the most likely one and check.
- Don't store images outside `assets/`.
- Don't use the model's vision to read text from an image when you can `Read` the source file directly.

User request: ${ARGUMENTS}
