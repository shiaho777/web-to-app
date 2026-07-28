# AI Coding

A prompt-driven coding assistant inside the app. Open it from [⋮ → AI Coding](/guide/main-screen/more).

## What it generates

Web apps, extension modules, userscripts, MV3 Chrome extensions, and local runtime projects.

## Features

- **Sessions** — each conversation has its own title and history.
- **Skills** — built-in skills guide generation: `debug`, `explain`, `optimize`, `refactor`, `i18n`, `imagery`, plus stack-specific skills (`react-app`, `nodejs-app`, `php-app`, `python-app`, `go-app`, `vue-app`, `wordpress-app`, `html-app`, `multi-web-app`) and module skills (`module-js`, `module-style`, `module-userscript`, `module-chrome-mv3`). You can edit or add skills in the skill editor.
- **Plan mode** — proposes a plan and waits for your approval before applying changes (shown with a plan-mode badge).
- **Resilience** — automatic retry with backoff on 429/5xx responses.

## Configuration

AI Coding uses the model and keys configured in [AI Settings](/guide/more-features/ai-settings).

## Notes

AI Coding produces *source*. To install a generated extension, save it through the [Extension Modules](/guide/more-features/extension-modules) flow.
