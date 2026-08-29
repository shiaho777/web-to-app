---
layout: home

hero:
  name: WebToApp
  text: Build Android APKs on your phone
  tagline: An on-device APK workshop that goes far beyond URL wrapping — fork+exec real server runtimes, ship a hardened network stack, and export Play-ready bundles. No PC required.
  image:
    src: /logo.png
    alt: WebToApp
  actions:
    - theme: brand
      text: Get Started
      link: /guide/introduction
    - theme: alt
      text: Developer Docs
      link: /developer/
    - theme: alt
      text: View on GitHub
      link: https://github.com/shiaho777/web-to-app

features:
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="4" width="16" height="16" rx="2"/><path d="M9 4v16"/><path d="M4 9h5"/></svg>
    title: Real on-device runtimes
    details: Node.js, PHP, Python, Go, and WordPress are fork+exec'd as native binaries straight from app storage — like Termux, packaged into an installable APK.
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3l8 3v6c0 4.5-3.2 7.6-8 9-4.8-1.4-8-4.5-8-9V6z"/></svg>
    title: Hardened networking
    details: DNS-over-HTTPS, TLS fingerprint spoofing with a local MITM bridge, Encrypted Client Hello (ECH), per-app proxies, and CORS bypass for locked-down SPAs.
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M21 8l-9-5-9 5 9 5z"/><path d="M3 8v8l9 5 9-5V8"/><path d="M12 13v8"/></svg>
    title: Self-contained builds
    details: Binary AXML/ARSC patching, permission pruning, V1/V2/V3 signing, and Google Play-ready AAB export — all inside the app via apksig. No remote build queue.
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M10 2v4M14 2v4M10 18v4M14 18v4M2 10h4M2 14h4M18 10h4M18 14h4"/><rect x="8" y="8" width="8" height="8" rx="1.5"/></svg>
    title: Extensible after shipping
    details: Add JS/CSS modules, Tampermonkey-style userscripts, or MV3 Chrome extensions (live-searched from the Chrome Web Store) without rebuilding the host.
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="10" width="16" height="10" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3"/><circle cx="12" cy="15" r="1.4"/></svg>
    title: Privacy & fingerprint defense
    details: 50+ vector browser fingerprint disguise, hosts-rule ad blocking with 20 built-in lists, AES-256-GCM resource encryption, and activation gating.
  - icon: <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M3 12h18"/><path d="M12 3a14 14 0 0 1 0 18a14 14 0 0 1 0-18"/></svg>
    title: 10 UI languages
    details: Chinese, English, Arabic (RTL), Portuguese, Spanish, French, German, Russian, Japanese, and Korean — switch anytime in Settings.
---

<div class="wta-home">

## From URL to a signed APK in three steps

<div class="wta-steps">

1. **Pick a type**

   Choose from [12 app types](/guide/app-types/) — a plain [Web](/guide/app-types/web) wrapper, [HTML](/guide/app-types/html) or [Frontend](/guide/app-types/frontend) builds, or on-device [Node.js](/guide/app-types/nodejs), [PHP](/guide/app-types/php), [Python](/guide/app-types/python), [Go](/guide/app-types/go), [WordPress](/guide/app-types/wordpress) servers.

2. **Fill in the basics**

   A name, a URL or a project, an icon — then save. Every type shares the same [configuration cards](/guide/config/) for network, privacy, appearance, and runtimes.

3. **Build and share**

   [Build APK](/guide/app-actions/build-apk) signs on-device with V1/V2/V3, then [share](/guide/app-actions/share-apk) it or [export a Play-ready AAB](/guide/app-actions/export-apk). No PC, no build queue.

</div>

## Twelve app types, one builder

<div class="wta-types">

<div class="wta-tile">

[**Web and Multi-Web**](/guide/app-types/multi-web)

URL wrappers, tabbed hubs, portals, and link feeds.

</div>

<div class="wta-tile">

[**HTML and Offline Pack**](/guide/app-types/html)

Package local HTML or zip builds, or scrape a site into a self-contained offline APK.

</div>

<div class="wta-tile">

[**Frontend**](/guide/app-types/frontend)

Ship React, Vue, or Vite builds as a localhost-served APK.

</div>

<div class="wta-tile">

[**Server runtimes**](/guide/app-types/nodejs)

fork+exec Node.js, PHP, Python, or Go binaries that serve on a local port.

</div>

<div class="wta-tile">

[**WordPress**](/guide/app-types/wordpress)

A full portable WordPress site with PHP and SQLite running on-device.

</div>

<div class="wta-tile">

[**Media and Gallery**](/guide/app-types/media)

Image and video players, albums, and portfolios as standalone apps.

</div>

</div>

## A toolbox behind the editor

<div class="wta-types">

<div class="wta-tile">

[**Agent**](/guide/more-features/agent)

A tool-calling assistant with 57 built-in tools that can build, edit, and operate the whole app.

</div>

<div class="wta-tile">

[**Extension modules**](/guide/more-features/extension-modules)

Inject JS/CSS, userscripts, or MV3 Chrome extensions into any generated app.

</div>

<div class="wta-tile">

[**Hosts ad-block**](/guide/more-features/hosts-adblock)

20 built-in filter lists and per-app subscriptions, compiled into the shipped APK.

</div>

<div class="wta-tile">

[**Linux environment**](/guide/more-features/linux-environment)

A Termux-style environment with real toolchains for building and running projects.

</div>

<div class="wta-tile">

[**Port manager**](/guide/more-features/port-manager)

Conflict policies, real stop handlers, and DNS bridging for every local server runtime.

</div>

<div class="wta-tile">

[**App modifier**](/guide/more-features/app-modifier)

Clone and rebrand installed APKs, batch-import definitions, export templates.

</div>

</div>

## Under the hood

<div class="wta-stats">

<div class="wta-stat"><b>12</b><span>app types</span></div>

<div class="wta-stat"><b>57</b><span>agent tools</span></div>

<div class="wta-stat"><b>10</b><span>UI languages</span></div>

<div class="wta-stat"><b>20</b><span>ad-filter lists</span></div>

</div>

The builder does its own binary patching — AXML/ARSC rewriting, permission pruning, AES-256-GCM resource encryption, 16 KB page-aligned native libraries — and keeps a low targetSdk shell so fork+exec runtimes keep working. The [developer docs](/developer/architecture) cover the full export pipeline.

<div class="wta-cta">

Ready to build your first APK?

[Get started](/guide/getting-started)

</div>

</div>
