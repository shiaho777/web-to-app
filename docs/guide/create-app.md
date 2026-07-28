# Creating an App

Tap **Create** on [My Apps](/guide/main-screen) to open a 3-column grid of app types. The types, in order:

| | | |
| --- | --- | --- |
| **Web** | **Multi-Web** | **HTML** |
| **Offline Pack** | **Frontend** | **PHP** |
| **WordPress** | **Node.js** | **Python** |
| **Go** | **Media** | **Gallery** |

Pick the one that matches your input. Each opens its own creation flow with type-specific fields, then shares the same set of [app configuration](/guide/config/) options.

## Choosing a type

| Type | Input | Output | Good for |
| --- | --- | --- | --- |
| [Web](/guide/app-types/web-content#web) | A URL | WebView-based APK | Landing pages, tools, dashboards, docs, internal systems |
| [Multi-Web](/guide/app-types/web-content#multi-web) | Several URLs | Tab/card/feed/drawer APK | Link hubs, portals, app collections |
| [HTML](/guide/app-types/web-content#html) | Local HTML files / zip | Localhost-backed APK | Static builds, offline web apps |
| [Offline Pack](/guide/app-types/web-content#offline-pack) | A URL (scraped) | Self-contained offline APK | Archiving a site for offline use |
| [Frontend](/guide/app-types/web-content#frontend) | A built front-end project | Localhost-backed APK | React, Vue, Vite production builds |
| [PHP](/guide/app-types/server-runtimes#php) | A PHP project | APK + on-device PHP server | Small PHP apps, admin tools |
| [WordPress](/guide/app-types/server-runtimes#wordpress) | A WordPress site | APK + PHP + SQLite | Portable sites, theme/plugin demos |
| [Node.js](/guide/app-types/server-runtimes#node-js) | A Node project | APK + on-device Node server | Express/Fastify/Koa apps, APIs |
| [Python](/guide/app-types/server-runtimes#python) | A Python project | APK + on-device Python server | Flask, Django, FastAPI, Tornado |
| [Go](/guide/app-types/server-runtimes#go) | A Go project | APK + on-device Go | Gin/Echo/Fiber, static serving |
| [Media](/guide/app-types/media#media) | An image or video | Media-player APK | Single-image/video viewers, course media |
| [Gallery](/guide/app-types/media#gallery) | A media collection | Gallery APK | Albums, portfolios, offline viewers |

## The creation flow

Every type follows the same shape:

1. **Type-specific form** — e.g. Web asks for a URL; Node.js asks for the project and start command; Gallery asks for media and layout. Runtime types (PHP/WordPress/Node.js/Python/Frontend) link to the [Linux Environment](/guide/more-features/dev-tools#linux-environment) for toolchain setup.
2. **Basic info** — name, icon.
3. **Save** — the app is created and appears on [My Apps](/guide/main-screen).

After creation, open the app's ⋮ menu and choose **Edit Core Config** to return to the type-specific form, or **Edit Common Config** for the shared options (appearance, networking, privacy, export). See [App Actions](/guide/app-actions) and [App Configuration](/guide/config/).

::: tip Try a sample first
The app bundles sample projects (React, Vue, Vite, Node/Express, PHP/Laravel, Python/Flask, Go/Gin, WordPress, and more). Use one to see a working configuration for your stack before building your own.
:::
