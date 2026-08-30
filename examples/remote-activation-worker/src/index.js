/**
 * WebToApp remote activation — reference verification server (Cloudflare Worker).
 *
 * Speaks the contract documented in `.github/docs/remote-activation.md` and
 * implemented client-side by `RemoteActivationVerifier.kt`. Two details in that
 * contract are easy to get wrong and will fail every activation if you do:
 *
 *   1. The signature field is `sig`, not `signature`.
 *   2. The signed payload is a JSON object whose keys are emitted in a fixed
 *      order — ok, expiresAt, remainingUses, nonce, then url — with null
 *      collapsed to 0 (expiresAt) and -1 (remainingUses). The client rebuilds
 *      the exact same string and verifies it, so a different key order or a
 *      different null fallback produces a signature mismatch.
 *
 * Storage is Workers KV. That is eventual-consistent and has no compare-and-
 * swap, so two devices redeeming the last seat of a device-bound code at the
 * same instant can both succeed. For anything where that matters, move the
 * record store to D1 or a Durable Object; the signing path is unchanged.
 */

const JSON_HEADERS = { "content-type": "application/json; charset=utf-8" };
const DEFAULT_CLOCK_SKEW_MS = 24 * 60 * 60 * 1000;

export default {
  async fetch(request, env) {
    if (request.method === "GET") return handleStatus(env);
    if (request.method === "POST") return handleVerify(request, env);
    return json({ error: "method not allowed" }, 405);
  },
};

/** GET / — config self-check. Never exposes the private key itself. */
async function handleStatus(env) {
  const problems = [];
  if (!env.SIGNING_KEY) problems.push("SIGNING_KEY is not set");
  if (!env.ACTIVATION_CODES) problems.push("ACTIVATION_CODES KV namespace is not bound");

  let publicKey = null;
  let fingerprint = null;
  if (env.SIGNING_KEY) {
    try {
      const spki = await publicSpki(await importSigningKey(env.SIGNING_KEY));
      // Hand back the public half so it can be pasted straight into the app's
      // "Signature public key" field. A public key is not a secret.
      publicKey = bytesToBase64(new Uint8Array(spki));
      fingerprint = await fingerprintOf(spki);
    } catch (error) {
      problems.push(`SIGNING_KEY is not a usable PKCS#8 EC P-256 key (${error.message})`);
    }
  }

  return json({
    ok: problems.length === 0,
    publicKey,
    keyFingerprint: fingerprint,
    urlEncryption: env.AES_KEY ? "enabled" : "disabled",
    problems,
  });
}

async function handleVerify(request, env) {
  if (!env.SIGNING_KEY) return json({ error: "server misconfigured: SIGNING_KEY missing" }, 500);
  if (!env.ACTIVATION_CODES) {
    return json({ error: "server misconfigured: ACTIVATION_CODES not bound" }, 500);
  }

  let body;
  try {
    body = await request.json();
  } catch {
    return json({ error: "invalid JSON body" }, 400);
  }

  const code = String(body.code ?? "").trim().toUpperCase();
  const deviceId = String(body.deviceId ?? "");
  const nonce = String(body.nonce ?? "");
  const packageName = String(body.packageName ?? "");
  const deviceBound = body.deviceBound === true;
  const timestamp = Number(body.ts);

  if (!code || !nonce) return json({ error: "code and nonce are required" }, 400);

  const skew = Number(env.MAX_CLOCK_SKEW_MS ?? DEFAULT_CLOCK_SKEW_MS);
  if (Number.isFinite(timestamp) && skew > 0) {
    const drift = Math.abs(Date.now() - timestamp);
    if (drift > skew) {
      // Not an error the client recovers from: it caches nothing, so the user
      // just sees the rejection message. Keeps stolen requests from replaying.
      return signed(env, {
        ok: false,
        message: "request timestamp too far from server time",
        nonce,
      });
    }
  }

  const record = await env.ACTIVATION_CODES.get(`code:${code}`, { type: "json" });
  if (!record) {
    return signed(env, { ok: false, message: "unknown code", nonce });
  }

  if (record.expiresAt && Date.now() > record.expiresAt) {
    return signed(env, { ok: false, message: "code expired", nonce, expiresAt: record.expiresAt });
  }

  if (record.packageName && packageName && record.packageName !== packageName) {
    return signed(env, { ok: false, message: "code not valid for this app", nonce });
  }

  let remainingUses = null;
  if (deviceBound) {
    const maxDevices = record.maxDevices ?? 1;
    const devices = Array.isArray(record.devices) ? record.devices : [];
    if (!devices.includes(deviceId) && devices.length >= maxDevices) {
      return signed(env, {
        ok: false,
        message: "code already in use on another device",
        nonce,
        expiresAt: record.expiresAt ?? null,
      });
    }
    if (!devices.includes(deviceId)) {
      record.devices = [...devices, deviceId];
      await env.ACTIVATION_CODES.put(`code:${code}`, JSON.stringify(record));
    }
    remainingUses = Math.max(0, maxDevices - record.devices.length);
  }

  // The request carries no flag telling us whether the app expects a delivered
  // URL, so the code record is the source of truth: a record with `url` must be
  // paired with an app that has "Deliver target URL" enabled, and a record
  // without one with an app that has it disabled. See the README.
  const includeUrl = typeof record.url === "string" && record.url.length > 0;
  let url = null;
  if (includeUrl) {
    if (record.encryptUrl) {
      if (!env.AES_KEY) {
        return json({ error: "code requires URL encryption but AES_KEY is not set" }, 500);
      }
      url = await encryptUrl(record.url, env.AES_KEY);
    } else {
      url = record.url;
    }
  }

  return signed(env, {
    ok: true,
    message: record.message ?? "",
    nonce,
    expiresAt: record.expiresAt ?? null,
    remainingUses,
    includeUrl,
    url,
  });
}

/**
 * Builds the response and signs it. `includeUrl` decides whether the URL joins
 * the signed payload, which must match the client's own deliverUrl setting.
 */
async function signed(env, options) {
  const {
    ok,
    message = "",
    nonce,
    expiresAt = null,
    remainingUses = null,
    includeUrl = false,
    url = null,
  } = options;

  const payload = canonicalSignedPayload({ ok, expiresAt, remainingUses, nonce, includeUrl, url });
  const signature = await signPayload(env.SIGNING_KEY, payload);

  const response = { ok, expiresAt, remainingUses, message, nonce, sig: signature };
  if (includeUrl) response.url = url;
  return json(response);
}

/**
 * Byte-for-byte reproduction of `RemoteActivationVerifier.canonicalSignedPayload`.
 * Built by hand rather than with JSON.stringify so the key order and the null
 * fallbacks cannot drift from the client.
 */
function canonicalSignedPayload({ ok, expiresAt, remainingUses, nonce, includeUrl, url }) {
  const fields = [
    `"ok":${ok === true}`,
    `"expiresAt":${expiresAt == null ? 0 : Math.trunc(expiresAt)}`,
    `"remainingUses":${remainingUses == null ? -1 : Math.trunc(remainingUses)}`,
    `"nonce":${JSON.stringify(nonce)}`,
  ];
  if (includeUrl) fields.push(`"url":${JSON.stringify(url ?? "")}`);
  return `{${fields.join(",")}}`;
}

async function signPayload(privateKeyBase64, payload) {
  const key = await importSigningKey(privateKeyBase64);
  const signature = await crypto.subtle.sign(
    { name: "ECDSA", hash: "SHA-256" },
    key,
    new TextEncoder().encode(payload)
  );
  // WebCrypto emits P1363 (r || s), which is what Java's SHA256withECDSA expects.
  return bytesToBase64(new Uint8Array(signature));
}

async function importSigningKey(privateKeyBase64) {
  return crypto.subtle.importKey(
    "pkcs8",
    base64ToBytes(privateKeyBase64),
    { name: "ECDSA", namedCurve: "P-256" },
    true,
    ["sign"]
  );
}

/**
 * SPKI encoding of the public half of a signing key.
 *
 * Goes through JWK rather than exporting "spki" from the private key directly:
 * that form is rejected by some WebCrypto implementations (Node, notably).
 */
async function publicSpki(privateKey) {
  const jwk = await crypto.subtle.exportKey("jwk", privateKey);
  const publicKey = await crypto.subtle.importKey(
    "jwk",
    { kty: jwk.kty, crv: jwk.crv, x: jwk.x, y: jwk.y },
    { name: "ECDSA", namedCurve: "P-256" },
    true,
    ["verify"]
  );
  return crypto.subtle.exportKey("spki", publicKey);
}

async function fingerprintOf(spki) {
  const digest = await crypto.subtle.digest("SHA-256", spki);
  return [...new Uint8Array(digest).slice(0, 8)]
    .map((byte) => byte.toString(16).padStart(2, "0"))
    .join("");
}

/** Wire format: Base64(IV[12] || ciphertext || GCM tag[16]). */
async function encryptUrl(plaintext, aesKeyBase64) {
  const key = await crypto.subtle.importKey("raw", base64ToBytes(aesKeyBase64), "AES-GCM", false, [
    "encrypt",
  ]);
  const iv = crypto.getRandomValues(new Uint8Array(12));
  const encrypted = new Uint8Array(
    await crypto.subtle.encrypt(
      { name: "AES-GCM", iv, tagLength: 128 },
      key,
      new TextEncoder().encode(plaintext)
    )
  );
  // WebCrypto already appends the tag to the ciphertext.
  const combined = new Uint8Array(iv.length + encrypted.length);
  combined.set(iv, 0);
  combined.set(encrypted, iv.length);
  return bytesToBase64(combined);
}

function base64ToBytes(base64) {
  const cleaned = String(base64).replace(/\s+/g, "");
  const binary = atob(cleaned);
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i += 1) bytes[i] = binary.charCodeAt(i);
  return bytes;
}

function bytesToBase64(bytes) {
  let binary = "";
  for (const byte of bytes) binary += String.fromCharCode(byte);
  return btoa(binary);
}

function json(body, status = 200) {
  return new Response(JSON.stringify(body), { status, headers: JSON_HEADERS });
}
