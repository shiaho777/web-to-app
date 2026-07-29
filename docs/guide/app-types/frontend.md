# Frontend

Packages a *built* front-end project — the production output of React, Vue, Vite, and similar frameworks.

## When to use

You have a framework project and want to ship its build output as an app.

## Core config

### Source

- **Build output** (`outputPath`) — the directory of the production build to package.
- **Framework** — detected or selected (React, Vue, Vite, etc.).

### Toolchain

- When a build step is needed on-device, the screen links to the [Linux Environment](/guide/more-features/linux-environment) to set up Node and build tools.

## Notes

- **Frontend vs HTML:** Frontend is for a framework project's build output; [HTML](/guide/app-types/html) is for plain static files you already have.
- The packaged front-end is served locally, so offline-capable SPAs work well.
