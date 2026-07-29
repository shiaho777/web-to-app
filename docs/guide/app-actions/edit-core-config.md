# Edit Core Config

Opens an app's **type-specific** settings — its source and runtime configuration. Tap ⋮ on an app card, then **Edit Core Config**.

## What it edits

The type-specific creation form, reused as an editor:

| App type | Core config covers |
| --- | --- |
| [Web](/guide/app-types/web) | *(Web has a single combined editor — see note)* |
| [Multi-Web](/guide/app-types/multi-web) | Sites, layout, per-site settings |
| [HTML](/guide/app-types/html) | Files, entry file, load mode, port |
| [Frontend](/guide/app-types/frontend) | Build output, framework |
| [PHP](/guide/app-types/php) | Project, start command |
| [WordPress](/guide/app-types/wordpress) | Site, themes, plugins |
| [Node.js](/guide/app-types/nodejs) | Project, start command |
| [Python](/guide/app-types/python) | Project, start command |
| [Go](/guide/app-types/go) | Module, build/run |
| [Media](/guide/app-types/media) | Media file, playback |
| [Gallery](/guide/app-types/gallery) | Media, view, playback |

## Notes

- **Web apps don't show this entry** — they have a single **Edit** that covers everything, since web config isn't split.
- For the shared options (appearance, networking, privacy, export), use [Edit Common Config](/guide/app-actions/edit-common-config/).
