# Port Manager

Coordinates the local-server ports used by runtime apps across all your generated apps. Open it from [⋮ → Port Manager](/guide/main-screen/more).

## Features

- **Service list** — see which ports are in use, the owning process, and latency.
- **Kill** — stop a single service, or **kill all**.
- **Auto-refresh** — keep the list current.
- **Port ranges** — view the configured port ranges.
- **Copy port / open** — quick actions per entry.
- **Filters** — show all services or filter; detects WebToApp apps specifically.
- **Status** — responding / not responding indicators.

## Conflict policy

When a runtime needs a port that's taken, the configured policy applies:

- `REASSIGN` — pick another port.
- `AUTO_KILL` — stop the conflicting service.
- `ALERT` — notify and let you decide.

## Notes

Runtimes allocate through the Port Manager and release their port on stop, so ports don't leak between apps.
