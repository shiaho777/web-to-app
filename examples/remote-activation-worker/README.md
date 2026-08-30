# Remote activation — reference verification server

A Cloudflare Worker that speaks the contract described in
[`.github/docs/remote-activation.md`](../../.github/docs/remote-activation.md).
It is the "I don't want to build a backend" answer to remote activation: copy,
paste, deploy.

What you get:

- Codes held in Workers KV, revocable and rotatable without rebuilding an APK
- Every response signed with **EC P-256 / SHA-256**, so a fake server or a
  man-in-the-middle cannot forge an `ok: true`
- Nonce echo (replay protection) and device binding for one-seat codes
- Optional dynamic URL delivery, with or without AES-256-GCM encryption

## 1. Generate a signing key

The app verifies responses with the public half; the Worker signs with the
private half.

```bash
# private key, PKCS#8, single-line Base64 — this is the Worker's SIGNING_KEY
openssl ecparam -genkey -name prime256v1 -noout -out ec.pem
openssl pkcs8 -topk8 -nocrypt -in ec.pem -out ec-pkcs8.pem
openssl base64 -A -in ec-pkcs8.pem -out ec-pkcs8.b64

# public key, SPKI — this goes into the app's "Signature public key" field
openssl ec -in ec.pem -pubout -out ec-pub.pem
openssl base64 -A -in ec-pub.pem -out ec-pub.b64
```

Keep `ec.pem` and `ec-pkcs8.b64` out of git. The public key is not a secret.

## 2. Deploy

```bash
cd examples/remote-activation-worker
npm install

npx wrangler login
npm run kv:create          # paste the printed id into wrangler.toml

npx wrangler secret put SIGNING_KEY < ec-pkcs8.b64
npx wrangler deploy
```

Then open the deployed URL in a browser. A healthy worker answers:

```json
{
  "ok": true,
  "publicKey": "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE…",
  "keyFingerprint": "3f9a1c02…",
  "urlEncryption": "disabled",
  "problems": []
}
```

`publicKey` is the SPKI Base64 of your key — paste it straight into the app's
**Signature public key** field. If `problems` is not empty, the worker will not
verify anything and will say why.

## 3. Add codes

One KV entry per code. The key is `code:` followed by the uppercased code.

```bash
# unlimited activations
npx wrangler kv:key put --binding=ACTIVATION_CODES "code:DEMO1234" \
  '{"note":"demo"}'

# expires at a fixed date (epoch millis)
npx wrangler kv:key put --binding=ACTIVATION_CODES "code:YEAR2027" \
  '{"expiresAt":1798761600000,"maxDevices":1,"devices":[]}'

# locked to one package, delivering an encrypted target URL
npx wrangler kv:key put --binding=ACTIVATION_CODES "code:VIP0001" \
  '{"packageName":"com.example.app","url":"https://example.com/target","encryptUrl":true}'
```

`encryptUrl` requires an AES key on the worker:

```bash
openssl rand -base64 32 | tr -d '\n' | xargs -0 npx wrangler secret put AES_KEY
```

### Code record fields

| Field | Type | Meaning |
| --- | --- | --- |
| `expiresAt` | number \| null | Absolute expiry, epoch millis. Omit for never. |
| `maxDevices` | number | Seats, enforced only when the app sends `deviceBound: true`. Default 1. |
| `devices` | string[] | Device ids that have redeemed this code. Maintained by the worker. |
| `packageName` | string \| null | If set, the code only works for that package. |
| `url` | string \| null | Enables URL delivery. See the caveat below. |
| `encryptUrl` | boolean | AES-256-GCM encrypt `url`. Requires `AES_KEY`. |
| `message` | string | Returned to the client on success. |
| `note` | string | Your own bookkeeping; never leaves the server. |

## Two things to know before you go live

**URL delivery is configured on both sides, and they must agree.** The request
the app sends does not say whether it expects a delivered URL, so the worker
cannot tell. The rule is therefore: a code with a `url` is for an app with
**Deliver target URL** enabled, and a code without one is for an app with it
disabled. Mismatched, the signature check fails on every activation, because the
app and the server disagree about whether the URL is part of the signed payload.

**Workers KV is eventually consistent and has no compare-and-swap.** Two devices
redeeming the last seat of a device-bound code in the same instant can both
succeed. For most apps this is an acceptable risk at this scale; if it is not for
yours, move the record store to D1 or a Durable Object — the signing path is
unchanged, only `get`/`put` differ.

## Verifying your changes

`test/protocol-check.mjs` drives the real worker with a mock KV and verifies each
response the way the Android client does — same payload construction, same
ECDSA P-256 check in P1363 form, same AES-GCM wire format.

```bash
npm run check
```

It is the fastest way to catch a change that would silently break every
installed app.

## Protocol details worth not breaking

Both live in `RemoteActivationVerifier.kt` on the app side, and both are
reproduced in `src/index.js`:

1. The signature field is **`sig`**, not `signature`.
2. The signed payload has a fixed key order — `ok`, `expiresAt`,
   `remainingUses`, `nonce`, then `url` when URL delivery is on — with `null`
   collapsed to `0` for `expiresAt` and `-1` for `remainingUses`.
