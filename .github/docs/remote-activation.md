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
| Deliver target URL | When on, the target URL is delivered by the server at activation time (not packaged in the APK). See [Dynamic URL delivery](#dynamic-url-delivery). |
| Encrypt target URL | When on (requires Deliver URL), the server must encrypt the URL with AES-256-GCM before signing. See [URL Encryption](#url-encryption-aes-256-gcm). |
| AES-256 key | 32-byte Base64 key shared with the server. Required when encryption is enabled. |

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
  "ts": 1733270400000,
  "deviceBound": false
}
```

- `code` is normalised (uppercased, trimmed) before sending.
- `deviceId` is a per-device identifier (not a hardware ID; it is a salted hash).
- `nonce` is fresh per request — you **must** echo it back unchanged (replay
  protection).
- `ts` is the client clock in epoch milliseconds.
- `deviceBound` is `true` when the entered code is a **device-bound** code. For
  such codes your server should enforce per-device binding: record the first
  `deviceId` that activates a given `code`, and reject the same `code` from any
  different `deviceId` (return `{ "ok": false }`). This is the only way to
  truly restrict a device-bound code to one device — the app itself has no
  shared state between devices, so a purely local device-bound code can only
  prevent re-activation on the same device after a local reset or hardware
  change.

## Response (your server → app)

```json
{
  "ok": true,
  "expiresAt": 1735862400000,
  "remainingUses": 5,
  "message": "",
  "nonce": "Base64-random-24-bytes",
  "sig": "Base64-ECDSA-signature",
  "url": "https://example.com/app"
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
| `url` | string \| null | Optional. The target URL to load. Only used (and signature-bound) when the app has **Deliver target URL from server** enabled — see [Dynamic URL delivery](#dynamic-url-delivery). When **Encrypt target URL** is also enabled, this field contains the AES-256-GCM ciphertext (see [URL Encryption](#url-encryption-aes-256-gcm)). Omit otherwise. |

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

When **Deliver target URL from server** is enabled, a fifth key `url` is
appended (after `nonce`), and the signature must cover it too:

```
{"ok":<true|false>,"expiresAt":<number>,"remainingUses":<number>,"nonce":"<nonce>","url":"<url>"}
```

`url` falls back to `""` when you omit it. Apps that do not enable URL delivery
verify the legacy four-key payload, so existing servers keep working unchanged.

Example of the precise bytes to sign:

```
{"ok":true,"expiresAt":1735862400000,"remainingUses":5,"nonce":"k7Q…"}
```

## Dynamic URL delivery

When **Deliver target URL from server** is enabled in the app, the target URL
is not packaged into the APK. Instead the app loads the URL you return in the
`url` field after a successful activation (and caches it for offline launches
under `ALLOW_CACHED`). This lets you change the URL without repackaging, and
keeps the URL out of the APK so it cannot be extracted statically.

- Return the URL in `url` and include it in the signed payload (fifth key).
- The URL is cached on the device after a successful online activation; offline
  launches reuse the cached URL per the offline policy.
- The URL is delivered in the clear inside the signed response (over HTTPS).
  This raises the bar against casual extraction but is not DRM-grade — a
  determined attacker can still recover it from the response or memory.

## URL Encryption (AES-256-GCM)

When both **Deliver target URL from server** and **Encrypt target URL** are
enabled, the server must encrypt the URL with AES-256-GCM **before** signing.
The ciphertext goes into the `url` field and the signed payload.

| Parameter | Value |
| --- | --- |
| Algorithm | AES-256-GCM (authenticated encryption) |
| Key | 32 bytes, shared between app config and server |
| IV | 12 random bytes per encryption |
| Wire format | `Base64(IV[12B] \|\| ciphertext \|\| GCM tag[16B])` |

**Why encrypt the URL when the response is already over HTTPS and ECDSA-signed?**
Defense in depth. If an attacker compromises the TLS layer (corporate MITM
proxy with a user-installed CA, misconfigured certificate pinning bypass) or
dumps the response from memory, the URL stays unreadable without the AES key.
The signature still prevents tampering, and the encryption prevents reading.

### Generating an AES-256 key

```bash
# Generate a 32-byte random key and Base64-encode it
openssl rand -base64 32
```

Paste the same Base64 string into both the app's **AES-256 key** field and
your server configuration.

### Server-side encryption (Node.js)

```js
const crypto = require("crypto");

function encryptUrl(plaintext, aesKeyBase64) {
  const key = Buffer.from(aesKeyBase64, "base64"); // 32 bytes
  const iv = crypto.randomBytes(12);
  const cipher = crypto.createCipheriv("aes-256-gcm", key, iv);
  let encrypted = cipher.update(plaintext, "utf8");
  encrypted = Buffer.concat([encrypted, cipher.final()]);
  const tag = cipher.getAuthTag(); // 16 bytes
  // Wire format: IV || ciphertext || tag
  return Buffer.concat([iv, encrypted, tag]).toString("base64");
}
```

### Signing flow with encryption

1. Encrypt the URL → Base64 ciphertext blob.
2. Build the canonical payload with the ciphertext blob as the `url` value.
3. Sign the canonical payload with ECDSA.
4. Return the ciphertext blob in the response `url` field and the signature in `sig`.

The client verifies the signature first (which covers the ciphertext), then
decrypts `url` with the shared AES key to recover the plaintext URL. An
attacker who tampers with the ciphertext breaks the signature; an attacker who
reads the response sees only encrypted bytes.

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
// `url` is optional: set it (and enable "Deliver target URL from server" in the
// app) to deliver the target URL dynamically instead of packaging it.
// When URL encryption is enabled, encrypt `url` with the shared AES key before
// returning it (see the `encryptUrl` helper below).
const CODES = {
  "ABC123": { expiresAt: 1735862400000, remainingUses: 5, url: "https://example.com/app" },
};

// ---- AES-256-GCM encryption (only needed when "Encrypt target URL" is on) ----
const AES_KEY_BASE64 = process.env.AES_KEY_BASE64 || ""; // same as in the app config
const AES_KEY = AES_KEY_BASE64 ? Buffer.from(AES_KEY_BASE64, "base64") : null;

function encryptUrl(plaintext) {
  if (!AES_KEY || AES_KEY.length !== 32) return plaintext; // passthrough if not configured
  const iv = crypto.randomBytes(12);
  const cipher = crypto.createCipheriv("aes-256-gcm", AES_KEY, iv);
  let encrypted = cipher.update(plaintext, "utf8");
  encrypted = Buffer.concat([encrypted, cipher.final()]);
  const tag = cipher.getAuthTag();
  return Buffer.concat([iv, encrypted, tag]).toString("base64");
}
// -------------------------------------------------------------------------

function signedPayload({ ok, expiresAt, remainingUses, nonce, url }) {
  // Must match the app's canonical order exactly.
  const payload = {
    ok,
    expiresAt: expiresAt ?? 0,
    remainingUses: remainingUses ?? -1,
    nonce,
  };
  // Include url only when delivering it; it becomes the fifth signed key.
  // When encryption is on, `url` is already the ciphertext blob here.
  if (url !== undefined) {
    payload.url = url ?? "";
  }
  return JSON.stringify(payload);
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

      // Encrypt the URL if AES key is configured; the ciphertext goes into
      // both the signed payload and the response.
      const urlPlain = entry && entry.url ? entry.url : undefined;
      const urlEnc = urlPlain ? encryptUrl(urlPlain) : undefined;

      const sig = sign(signedPayload({ ...result, nonce, url: urlEnc }));

      res.writeHead(200, { "Content-Type": "application/json" });
      res.end(JSON.stringify({ ...result, nonce, sig, url: urlEnc }));
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
