# Create App

Tap [Create](/guide/main-screen/create-app) on My Apps to open the app-type picker. Choose the type that matches your input; each opens its own creation flow, then shares the same [app configuration](/guide/config/) options.

## The 12 types

| Type | Input | Output | Good for |
| --- | --- | --- | --- |
| [Web](/guide/app-types/web) | A URL | WebView APK | Landing pages, tools, dashboards, docs |
| [Multi-Web](/guide/app-types/multi-web) | Several URLs | Tab/card/feed/drawer APK | Link hubs, portals |
| [HTML](/guide/app-types/html) | Local HTML / zip | Localhost APK | Static builds, offline web apps |
| [Offline Pack](/guide/app-types/offline-pack) | A URL (scraped) | Self-contained offline APK | Archiving a site |
| [Frontend](/guide/app-types/frontend) | Built front-end project | Localhost APK | React, Vue, Vite builds |
| [PHP](/guide/app-types/php) | PHP project | APK + on-device PHP | Small PHP apps |
| [WordPress](/guide/app-types/wordpress) | WordPress site | APK + PHP + SQLite | Portable sites |
| [Node.js](/guide/app-types/nodejs) | Node project | APK + on-device Node | Express/Fastify/Koa, APIs |
| [Python](/guide/app-types/python) | Python project | APK + on-device Python | Flask, Django, FastAPI |
| [Go](/guide/app-types/go) | Go project | APK + on-device Go | Gin/Echo/Fiber |
| [Media](/guide/app-types/media) | An image or video | Media-player APK | Single media viewers |
| [Gallery](/guide/app-types/gallery) | A media collection | Gallery APK | Albums, portfolios |

## The creation flow

Every type follows the same shape:

1. **Type-specific form** — e.g. Web asks for a URL; Node.js asks for the project and start command; Gallery asks for media and layout. Runtime types link to the [Linux Environment](/guide/more-features/linux-environment) for toolchain setup.
2. **Basic info** — name and icon.
3. **Save** — the app is created and appears on [My Apps](/guide/main-screen/my-apps).

After creation, use the app's ⋮ menu: [Edit Core Config](/guide/app-actions/edit-core-config) returns to the type-specific form; [Edit Common Config](/guide/app-actions/edit-common-config/) opens the shared options.

::: tip Try a sample first
The app bundles sample projects (React, Vue, Vite, Node/Express, PHP/Laravel, Python/Flask, Go/Gin, WordPress, and more). Use one to see a working configuration for your stack.
:::
