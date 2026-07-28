# FAQ

## Is WebToApp free?

Yes. WebToApp is open source under [The Unlicense](https://github.com/shiaho777/web-to-app/blob/main/LICENSE).

## What Android version do I need?

Android 6.0 (API 23) or newer.

## Do I need a PC to build apps?

No. The entire build — binary patching, signing, and AAB export — happens on-device. A PC is only needed if you want to build WebToApp itself from source.

## Why do generated apps target SDK 28?

The low `targetSdk` is deliberate: it lets generated apps fork+exec native runtimes (Node.js, PHP, Python, Go, WordPress) from app storage. The AAB exporter separately rewrites `targetSdk` for Play Store distribution.

## A feature works in preview but not after export. Why?

Usually a config field did not flow through the export chain (model → `ApkConfig` JSON → shell config → runtime). See [Config Field Drift](/developer/config-drift) for the diagnosis checklist.

## Can I run browser extensions?

Yes. WebToApp supports built-in JS/CSS modules, Tampermonkey-style userscripts, and MV3 Chrome extensions. The **Browser Extensions** tab searches the Chrome Web Store live. See [Extension Authoring](/extensions/).

## How do I publish a module to the market?

Add a folder under `modules/`, update `registry.json`, and open a pull request. See [Publish to the Market](/extensions/publish).

## Where do I get help?

- GitHub: [github.com/shiaho777/web-to-app](https://github.com/shiaho777/web-to-app)
- Telegram: [t.me/webtoapp777](https://t.me/webtoapp777)
- X (Twitter): [@shiaho777](https://x.com/shiaho777)
