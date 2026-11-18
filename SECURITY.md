# Security Policy

## Supported versions

Security fixes land on `main` and ship in the latest release. Older releases do not receive backported patches — update to the latest version before reporting.

| Version | Supported |
| --- | --- |
| Latest release | Yes |
| Older releases | No |

## Reporting a vulnerability

**Please do not open a public issue for security problems.**

Use GitHub's private vulnerability reporting instead:

1. Open https://github.com/shiaho777/web-to-app/security/advisories/new
2. Describe the issue, the affected version, and reproduction steps.
3. Attach a proof of concept if you have one.

Reports are visible only to the maintainers. If the form is unavailable, contact the maintainer through the channels listed in the README and ask for a private channel before sharing details.

## What to expect

- Acknowledgement within 7 days.
- A fix or a documented mitigation as soon as practical; you will be kept in the loop on progress.
- Credit in the release notes and the advisory, unless you prefer to remain anonymous.

## Scope

This policy covers the WebToApp host app, the shell template, and the export pipeline in this repository.

Out of scope:

- APKs, websites, or servers that users build with WebToApp — report those to their own operators.
- Vulnerabilities in upstream runtimes (Node.js, PHP, Python, Go, WordPress) or engines (GeckoView, Cronet) — report those upstream; we track and ship updated versions.
- Issues that require a malicious app already installed on the device with elevated privileges.
