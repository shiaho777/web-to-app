# Python

Runs a Python project on an on-device Python server; the WebView points at the local port.

## When to use

Flask, Django, FastAPI, Tornado apps, or the built-in HTTP server.

## Runtime

- **Version** — Python 3.14.
- **Frameworks** — Flask, Django, FastAPI (uvicorn), Tornado, built-in HTTP server.
- **Dependencies** — pip resolves into `.pypackages`; custom native extensions supported.
- **Versioning** — binary names are versioned so future bumps don't hard-code paths.
- Managed in the [Linux Environment](/guide/more-features/linux-environment) and [Runtime Management](/guide/more-features/runtime-management) screens.

## Key configuration

- **Project** — the Python source.
- **Start command** — e.g. `python app.py` or `uvicorn main:app`.
- **Port** — allocated through the [Port Manager](/guide/more-features/port-manager).
