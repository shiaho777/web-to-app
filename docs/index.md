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
  - icon: ⚙️
    title: Real on-device runtimes
    details: Node.js, PHP, Python, Go, and WordPress are fork+exec'd as native binaries straight from app storage — like Termux, packaged into an installable APK.
  - icon: 🛡️
    title: Hardened networking
    details: DNS-over-HTTPS, TLS fingerprint spoofing with a local MITM bridge, Encrypted Client Hello (ECH), per-app proxies, and CORS bypass for locked-down SPAs.
  - icon: 📦
    title: Self-contained builds
    details: Binary AXML/ARSC patching, permission pruning, V1/V2/V3 signing, and Google Play-ready AAB export — all inside the app via apksig. No remote build queue.
  - icon: 🧩
    title: Extensible after shipping
    details: Add JS/CSS modules, Tampermonkey-style userscripts, or MV3 Chrome extensions (live-searched from the Chrome Web Store) without rebuilding the host.
  - icon: 🔒
    title: Privacy & fingerprint defense
    details: 50+ vector browser fingerprint disguise, hosts-rule ad blocking with 20 built-in lists, AES-256-GCM resource encryption, and activation gating.
  - icon: 🌍
    title: 10 UI languages
    details: Chinese, English, Arabic (RTL), Portuguese, Spanish, French, German, Russian, Japanese, and Korean — switch anytime in Settings.
---
