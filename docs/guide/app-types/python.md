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

## Core config

Backed by `PythonAppConfig`.

### Project

- **Project** (`projectId`/`projectName`, `sourceProjectPath`) — the Python source.
- **Framework** (`framework`) — detected framework, if any.
- **Python version** (`pythonVersion`).

### Entry & server

- **Entry file** (`entryFile`) — defaults to `app.py`.
- **Entry module** (`entryModule`) — for module-style entry (e.g. `main:app`).
- **Server type** (`serverType`) — `builtin`, uvicorn, etc.
- **Port** (`serverPort`) — allocated through the [Port Manager](/guide/more-features/port-manager).
- **Environment variables** (`envVars`) — key/value pairs passed to the process.

### Dependencies & extensions

- **Requirements file** (`requirementsFile`) — defaults to `requirements.txt`.
- **Has pip dependencies** (`hasPipDeps`) — whether deps resolve into `.pypackages`.
- **Custom native extensions** (`customPythonExtensions`) — add `.so` extensions, each with load order.
