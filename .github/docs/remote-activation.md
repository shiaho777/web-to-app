# Remote Activation — Server Contract

WebToApp can verify activation codes against **your own HTTPS endpoint**
instead of (or in addition to) the codes baked into the APK. This lets you
revoke codes, issue them dynamically, and control usage centrally — without
rebuilding the app.

> **Security note.** The host is open source and verification runs on the
> client, so a determined attacker can still patch the check out. Remote
> verification *raises the bar* and gives you revocation/rotation; it is not
> an anti-cracking guarantee. The response signature below stops a fake server
> or a man-in-the-middle from forging an `ok: true`, which is the part that
> matters most.

## How it is configured in the app

When building an app (or in App Modifier), enable **Online Verification** under
the activation-code section and fill in:

| Field | Meaning |
| --- | --- |
| Verification endpoint | Your `https://…` URL. Plain `http://` is rejected. |
| Signature public key | An **EC P-256** public key in Base64 (SPKI / `BEGIN PUBLIC KEY`). The app verifies every response with it. |
| Offline policy | `ALLOW_CACHED` (default): allow the last successful result until it expires. `DENY`: block when offline. `ALLOW`: always allow when offline (insecure). |

## Request (app → your server)

```
POST <your verification endpoint>
Content-Type: application/json; charset=utf-8
Accept: application/json
```

```json
{
  "code": "ABC123",
  "deviceId": "f3a9…",
  "packageName": "com.example.app",
  "nonce": "Base64-random-24-bytes",
  "ts": 1733270400000
}
```

- `code` is normalised (uppercased, trimmed) before sending.
- `deviceId` is a per-device identifier (not a hardware ID; it is a salted hash).
- `nonce` is fresh per request — you **must** echo it back unchanged (replay
  protection).
- `ts` is the client clock in epoch milliseconds.

## Response (your server → app)

```json
{
  "ok": true,
  "expiresAt": 1735862400000,
  "remainingUses": 5,
  "message": "",
  "nonce": "Base64-random-24-bytes",
  "sig": "Base64-ECDSA-signature"
}
```

| Field | Type | Notes |
| --- | --- | --- |
| `ok` | boolean | `true` grants activation. |
| `expiresAt` | number \| null | Epoch ms. If present and in the past, the app treats it as expired. Use `0`/omit for "never expires". |
| `remainingUses` | number \| null | Informational. Use `-1`/omit when not tracking. |
| `message` | string | Shown to the user on rejection. |
| `nonce` | string | Must equal the request nonce. |
| `sig` | string | Base64 ECDSA (`SHA256withECDSA`, DER-encoded) over the canonical payload below. |

### Canonical payload that gets signed

The app rebuilds this exact JSON string and verifies `sig` against it. **Key
order and formatting matter** — it is compact JSON with these four keys, in
this order:

```
{"ok":<true|false>,"expiresAt":<number>,"remainingUses":<number>,"nonce":"<nonce>"}
```

- `expiresAt` falls back to `0` when you omit it.
- `remainingUses` falls back to `-1` when you omit it.
- No spaces, booleans unquoted, numbers unquoted.

Example of the precise bytes to sign:

```
{"ok":true,"expiresAt":1735862400000,"remainingUses":5,"nonce":"k7Q…"}
```

## Generating a key pair

```bash
# private key (keep on your server)
openssl ecparam -name prime256v1 -genkey -noout -out ec_private.pem
# public key (paste into the app's "Signature public key" field, header lines optional)
openssl ec -in ec_private.pem -pubout -out ec_public.pem
```

## Reference server (Node.js, no framework)

```js
const http = require("http");
const crypto = require("crypto");
const fs = require("fs");

const privateKey = crypto.createPrivateKey(fs.readFileSync("ec_private.pem"));

// Your own source of truth. Revoke by removing/flipping entries here.
const CODES = {
  "ABC123": { expiresAt: 1735862400000, remainingUses: 5 },
};

function signedPayload({ ok, expiresAt, remainingUses, nonce }) {
  // Must match the app's canonical order exactly.
  return JSON.stringify({
    ok,
    expiresAt: expiresAt ?? 0,
    remainingUses: remainingUses ?? -1,
    nonce,
  });
}

function sign(payloadString) {
  return crypto
    .sign("sha256", Buffer.from(payloadString, "utf8"), privateKey)
    .toString("base64");
}

http
  .createServer((req, res) => {
    let body = "";
    req.on("data", (c) => (body += c));
    req.on("end", () => {
      let parsed;
      try {
        parsed = JSON.parse(body);
      } catch {
        res.writeHead(400);
        return res.end();
      }

      const code = String(parsed.code || "").trim().toUpperCase();
      const nonce = String(parsed.nonce || "");
      const entry = CODES[code];

      const result = entry
        ? { ok: true, expiresAt: entry.expiresAt, remainingUses: entry.remainingUses, message: "" }
        : { ok: false, expiresAt: 0, remainingUses: -1, message: "Code not recognised" };

      const sig = sign(signedPayload({ ...result, nonce }));

      res.writeHead(200, { "Content-Type": "application/json" });
      res.end(JSON.stringify({ ...result, nonce, sig }));
    });
  })
  .listen(8443);
```

Serve it over HTTPS (behind a reverse proxy with a TLS cert, or with
`https.createServer`). The app refuses non-HTTPS endpoints.

## Offline behaviour

After a successful online check, the app caches the result (bound to the code
and its `expiresAt`). With `ALLOW_CACHED`, a later offline launch is allowed
until that expiry, then it prompts again. With `DENY`, any offline launch is
blocked. With `ALLOW`, offline launches always pass — only use this if losing
the gate offline is acceptable.

## Privacy

When online verification is on, the app sends the activation code and a device
identifier to the endpoint you configure. Disclose this to your users.
