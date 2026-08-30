/**
 * Protocol conformance check: drive the real Worker with a mock KV and verify
 * the response the way the Android client does.
 *
 * The client path being reproduced is RemoteActivationVerifier.verifySignature:
 *   - the signed payload is rebuilt from the response fields
 *   - the signature is ECDSA / SHA-256 / P-256 in P1363 (r || s) form, which is
 *     what Java's "SHA256withECDSA" consumes
 *   - the nonce must match the one the request sent
 *
 * Run with: npm run check   (or: node test/protocol-check.mjs)
 */

import assert from "node:assert/strict";
import crypto from "node:crypto";
import { webcrypto } from "node:crypto";
import worker from "../src/index.js";

// Node 18 exposes WebCrypto globally, but be explicit in case it is stripped.
if (!globalThis.crypto) globalThis.crypto = webcrypto;

const tests = [];
function test(name, fn) {
  tests.push([name, fn]);
}

/** Minimal Workers KV stand-in. */
function mockKv(initial = {}) {
  const store = new Map(Object.entries(initial));
  return {
    store,
    async get(key, options) {
      const value = store.get(key);
      if (value === undefined) return null;
      return options?.type === "json" ? JSON.parse(value) : value;
    },
    async put(key, value) {
      store.set(key, value);
    },
  };
}

async function generateSigningKey() {
  const pair = await crypto.subtle.generateKey({ name: "ECDSA", namedCurve: "P-256" }, true, [
    "sign",
    "verify",
  ]);
  const pkcs8 = Buffer.from(await crypto.subtle.exportKey("pkcs8", pair.privateKey)).toString(
    "base64"
  );
  const spki = Buffer.from(await crypto.subtle.exportKey("spki", pair.publicKey));
  return { pkcs8, spki };
}

function publicKeyFromSpki(spki) {
  return crypto.createPublicKey({ key: spki, format: "der", type: "spki" });
}

/**
 * Rebuilds the payload exactly as RemoteActivationVerifier.canonicalSignedPayload
 * does on the client, then verifies the signature from the response.
 */
function verifyLikeClient(publicKey, response, expectedNonce, includeUrl) {
  assert.ok(response.sig, "response is missing the `sig` field");
  assert.equal(response.nonce, expectedNonce, "nonce was not echoed back");

  const fields = [
    `"ok":${response.ok === true}`,
    `"expiresAt":${response.expiresAt == null ? 0 : Math.trunc(response.expiresAt)}`,
    `"remainingUses":${response.remainingUses == null ? -1 : Math.trunc(response.remainingUses)}`,
    `"nonce":${JSON.stringify(response.nonce)}`,
  ];
  if (includeUrl) fields.push(`"url":${JSON.stringify(response.url ?? "")}`);

  const payload = `{${fields.join(",")}}`;
  const signature = Buffer.from(response.sig, "base64");
  return crypto.verify(
    "sha256",
    Buffer.from(payload, "utf8"),
    { key: publicKey, dsaEncoding: "ieee-p1363" },
    signature
  );
}

/** Mirrors RemoteActivationVerifier.decryptUrl: Base64(IV[12] || ct || tag[16]). */
function decryptUrlLikeClient(encryptedBase64, aesKeyBase64) {
  const combined = Buffer.from(encryptedBase64, "base64");
  const iv = combined.subarray(0, 12);
  const tag = combined.subarray(combined.length - 16);
  const ciphertext = combined.subarray(12, combined.length - 16);
  const decipher = crypto.createDecipheriv("aes-256-gcm", Buffer.from(aesKeyBase64, "base64"), iv);
  decipher.setAuthTag(tag);
  return Buffer.concat([decipher.update(ciphertext), decipher.final()]).toString("utf8");
}

async function post(body, env) {
  const response = await worker.fetch(
    new Request("https://activation.example.com/verify", {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify(body),
    }),
    env
  );
  return { status: response.status, body: await response.json() };
}

test("a valid code produces a signature the client accepts", async () => {
  const { pkcs8, spki } = await generateSigningKey();
  const expiresAt = Date.now() + 86_400_000;
  const env = {
    SIGNING_KEY: pkcs8,
    ACTIVATION_CODES: mockKv({
      "code:DEMO1234": JSON.stringify({ expiresAt, maxDevices: 1, devices: [] }),
    }),
  };

  const { status, body } = await post(
    { code: "demo1234", deviceId: "device-a", packageName: "com.example.app", nonce: "nonce-1", ts: Date.now() },
    env
  );

  assert.equal(status, 200);
  assert.equal(body.ok, true);
  assert.equal(body.expiresAt, expiresAt);
  assert.ok(verifyLikeClient(publicKeyFromSpki(spki), body, "nonce-1", false));
});

test("unknown code is signed as a rejection rather than failing open", async () => {
  const { pkcs8, spki } = await generateSigningKey();
  const env = { SIGNING_KEY: pkcs8, ACTIVATION_CODES: mockKv() };

  const { body } = await post({ code: "NOPE", deviceId: "d", nonce: "nonce-2", ts: Date.now() }, env);

  assert.equal(body.ok, false);
  assert.ok(verifyLikeClient(publicKeyFromSpki(spki), body, "nonce-2", false));
});

test("an expired code is rejected", async () => {
  const { pkcs8, spki } = await generateSigningKey();
  const env = {
    SIGNING_KEY: pkcs8,
    ACTIVATION_CODES: mockKv({
      "code:OLD": JSON.stringify({ expiresAt: Date.now() - 1000 }),
    }),
  };

  const { body } = await post({ code: "OLD", deviceId: "d", nonce: "nonce-3", ts: Date.now() }, env);

  assert.equal(body.ok, false);
  assert.match(body.message, /expired/);
  assert.ok(verifyLikeClient(publicKeyFromSpki(spki), body, "nonce-3", false));
});

test("a device-bound code rejects a second device", async () => {
  const { pkcs8, spki } = await generateSigningKey();
  const env = {
    SIGNING_KEY: pkcs8,
    ACTIVATION_CODES: mockKv({
      "code:ONESEAT": JSON.stringify({ maxDevices: 1, devices: ["device-a"] }),
    }),
  };

  const first = await post({ code: "ONESEAT", deviceId: "device-a", nonce: "n-a", ts: Date.now(), deviceBound: true }, env);
  assert.equal(first.body.ok, true, "the bound device may re-verify");
  assert.equal(first.body.remainingUses, 0);
  assert.ok(verifyLikeClient(publicKeyFromSpki(spki), first.body, "n-a", false));

  const second = await post({ code: "ONESEAT", deviceId: "device-b", nonce: "n-b", ts: Date.now(), deviceBound: true }, env);
  assert.equal(second.body.ok, false);
  assert.match(second.body.message, /another device/);
  assert.ok(verifyLikeClient(publicKeyFromSpki(spki), second.body, "n-b", false));
});

test("a delivered url is inside the signed payload", async () => {
  const { pkcs8, spki } = await generateSigningKey();
  const env = {
    SIGNING_KEY: pkcs8,
    ACTIVATION_CODES: mockKv({
      "code:WITHURL": JSON.stringify({ url: "https://example.com/target" }),
    }),
  };

  const { body } = await post({ code: "WITHURL", deviceId: "d", nonce: "nonce-4", ts: Date.now() }, env);

  assert.equal(body.ok, true);
  assert.equal(body.url, "https://example.com/target");
  // Client has Deliver URL enabled, so its payload includes the url field.
  assert.ok(verifyLikeClient(publicKeyFromSpki(spki), body, "nonce-4", true));
  // And the legacy payload (without url) must NOT verify — proving the url is
  // actually protected and not merely riding along unsigned.
  assert.equal(verifyLikeClient(publicKeyFromSpki(spki), body, "nonce-4", false), false);
});

test("an encrypted url decrypts with the format the client expects", async () => {
  const { pkcs8, spki } = await generateSigningKey();
  const aesKey = crypto.randomBytes(32).toString("base64");
  const env = {
    SIGNING_KEY: pkcs8,
    AES_KEY: aesKey,
    ACTIVATION_CODES: mockKv({
      "code:SECURE": JSON.stringify({ url: "https://example.com/secret", encryptUrl: true }),
    }),
  };

  const { body } = await post({ code: "SECURE", deviceId: "d", nonce: "nonce-5", ts: Date.now() }, env);

  assert.equal(body.ok, true);
  assert.notEqual(body.url, "https://example.com/secret", "url must not be sent in the clear");
  assert.ok(verifyLikeClient(publicKeyFromSpki(spki), body, "nonce-5", true));
  assert.equal(decryptUrlLikeClient(body.url, aesKey), "https://example.com/secret");
});

test("a stale timestamp is rejected", async () => {
  const { pkcs8, spki } = await generateSigningKey();
  const env = {
    SIGNING_KEY: pkcs8,
    MAX_CLOCK_SKEW_MS: "60000",
    ACTIVATION_CODES: mockKv({ "code:FRESH": JSON.stringify({}) }),
  };

  const stale = Date.now() - 3_600_000;
  const { body } = await post({ code: "FRESH", deviceId: "d", nonce: "nonce-6", ts: stale }, env);

  assert.equal(body.ok, false);
  assert.match(body.message, /timestamp/);
  assert.ok(verifyLikeClient(publicKeyFromSpki(spki), body, "nonce-6", false));
});

test("GET returns a usable public key and no secrets", async () => {
  const { pkcs8, spki } = await generateSigningKey();
  const response = await worker.fetch(new Request("https://activation.example.com/"), {
    SIGNING_KEY: pkcs8,
    ACTIVATION_CODES: mockKv(),
  });
  const body = await response.json();

  assert.equal(body.ok, true);
  assert.deepEqual(body.problems, []);
  assert.match(body.keyFingerprint, /^[0-9a-f]{16}$/);
  assert.equal(body.urlEncryption, "disabled");

  // The point of the endpoint: the operator can copy this straight into the
  // app's "Signature public key" field, so it must be the real counterpart.
  const returned = crypto.createPublicKey({
    key: Buffer.from(body.publicKey, "base64"),
    format: "der",
    type: "spki",
  });
  assert.equal(
    returned.export({ type: "spki", format: "der" }).toString("base64"),
    spki.toString("base64"),
    "GET must return the public half of SIGNING_KEY"
  );

  assert.equal(JSON.stringify(body).includes(pkcs8), false, "private key must never be returned");
});

test("GET reports a missing KV binding instead of failing silently", async () => {
  const { pkcs8 } = await generateSigningKey();
  const response = await worker.fetch(new Request("https://activation.example.com/"), {
    SIGNING_KEY: pkcs8,
  });
  const body = await response.json();

  assert.equal(body.ok, false);
  assert.ok(
    body.problems.some((problem) => problem.includes("ACTIVATION_CODES")),
    `expected a KV problem, got ${JSON.stringify(body.problems)}`
  );
});

let failures = 0;
for (const [name, fn] of tests) {
  try {
    await fn();
    console.log(`  ok    ${name}`);
  } catch (error) {
    failures += 1;
    console.error(`  FAIL  ${name}\n        ${error.message}`);
  }
}

console.log(`\n${tests.length - failures}/${tests.length} passed`);
process.exit(failures === 0 ? 0 : 1);
