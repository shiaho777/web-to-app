package com.webtoapp.core.featurestack.api

import android.app.Activity
import android.content.Context
import android.content.Intent
import org.json.JSONObject
import java.io.OutputStream

/**
 * Contract between the main runtime and feature-stack implementations.
 *
 * Heavy optional dependency stacks (Google sign-in / Credential Manager, FCM /
 * Firebase, Cronet HTTP/3) are compiled into `assets/feature_stacks/<id>.dex`
 * archives so a generated APK can leave them out entirely when the corresponding
 * build option is off. The dex code is loaded through a `DexClassLoader` whose
 * parent is the app class loader, so everything referenced across the boundary
 * must be one of:
 *
 *  - platform types (`android.*`, `java.*`, `org.json.*`),
 *  - these API types (kept unrenamed in both app and shell ProGuard configs),
 *  - Kotlin stdlib / kotlinx-coroutines (keepnames-pinned in the shell config).
 *
 * Feature implementations must never reference other `com.webtoapp.*` classes —
 * those are renamed under R8 and would not resolve inside a generated APK.
 * Anything the host runtime offers (logging, notifications, downloads, strings)
 * is reachable through [FeatureRuntime].
 */
interface FeatureStack {

    /** Called once by [com.webtoapp.core.featurestack.FeatureStackLoader] before use. */
    fun init(context: Context, runtime: FeatureRuntime)
}

/** Host-runtime services exposed to feature-stack dex code. */
interface FeatureRuntime {

    companion object {
        const val LOG_DEBUG = 0
        const val LOG_INFO = 1
        const val LOG_WARN = 2
        const val LOG_ERROR = 3
    }

    fun log(level: Int, tag: String, message: String, throwable: Throwable?)

    /** Routes to Strings.* on the main side; impls must not reference Strings directly. */
    fun localizedString(key: String): String

    fun ensureNotificationChannel(context: Context, channelId: String, name: String, description: String)

    fun showNotification(context: Context, channelId: String, title: String, body: String, clickUrl: String?)

    /** App display name configured for FCM notifications (may be blank). */
    fun fcmAppName(context: Context): String

    /** Fallback click URL configured for FCM notifications (may be null). */
    fun fcmClickUrl(context: Context): String?

    /**
     * Downloads the first reachable URL to [destPath], retrying per URL and
     * verifying the SHA-256 when [sha256Hex] is provided. Used by stacks whose
     * native payloads are fetched on demand instead of being packaged.
     */
    fun downloadFile(
        context: Context,
        urls: List<String>,
        destPath: String,
        sha256Hex: String?,
        displayName: String,
        maxRetryPerUrl: Int,
        retryDelayMs: Long
    ): Boolean
}

/** Result of a native Google sign-in attempt; [payload] carries `ok` plus tokens/errors. */
interface SignInCallback {
    fun onResult(requestId: String, payload: JSONObject)
}

/** FCM events the implementation reports back to the main runtime. */
interface FcmEventSink {
    fun onToken(token: String)
}

/** Native Google sign-in via Credential Manager. */
interface GoogleSignInStack : FeatureStack {
    fun signIn(activity: Activity, requestId: String, clientId: String, callback: SignInCallback)
}

/** FCM stack: Firebase init, token fetch and incoming-message handling. */
interface FcmStack : FeatureStack {

    /** Initializes Firebase for this app and fetches the current token into [sink]. */
    fun start(config: FcmConfig, sink: FcmEventSink)

    /**
     * Handles a raw push intent received by the proxy service. [intent] carries the
     * extras GMS sent (the impl wraps them into a RemoteMessage internally).
     */
    fun onMessagingEvent(intent: Intent, sink: FcmEventSink)

    /** Re-reads the current token (cheap; used when the app regains foreground). */
    fun refreshToken(sink: FcmEventSink)
}

/** Firebase coordinates needed to initialize FirebaseApp manually. */
class FcmConfig(
    val projectId: String,
    val applicationId: String,
    val apiKey: String,
    val senderId: String
)

/**
 * Cronet artifact constants shared between the main-side native-lib injector
 * (ApkBuilder → CronetDependencyManager) and the dex-packaged forwarder —
 * single source so version bumps cannot drift apart.
 */
object CronetSpec {
    /** Gradle artifact version (app/shell/feature-stacks build.gradle.kts must match). */
    const val ARTIFACT_VERSION = "143.7445.0"

    /** Native library version baked inside the artifact (differs from the artifact version). */
    const val LIB_VERSION = "143.0.7445.0"

    const val LIB_FILE_NAME = "libcronet.$LIB_VERSION.so"

    /**
     * SHA-256 of cronet-embedded-$ARTIFACT_VERSION.aar (canonical bytes from
     * Google Maven; Aliyun mirrors the same artifact). Recompute when bumping
     * ARTIFACT_VERSION — mismatches fail the download loudly.
     */
    const val AAR_SHA256 =
        "afdd7af7568e9758e3690c3a4a228f30993ac3e4ef0f94c72bb68503c0167697"

    const val GOOGLE_MAVEN_URL =
        "https://maven.google.com/org/chromium/net/cronet-embedded/$ARTIFACT_VERSION/cronet-embedded-$ARTIFACT_VERSION.aar"

    /** Aliyun's Google-Maven mirror — listed first (see downloadUrls). */
    const val CN_MIRROR_URL =
        "https://maven.aliyun.com/repository/google/org/chromium/net/cronet-embedded/$ARTIFACT_VERSION/cronet-embedded-$ARTIFACT_VERSION.aar"

    /** Aliyun first (mainland reachability), Google Maven as the fallback. */
    val downloadUrls: List<String> = listOf(CN_MIRROR_URL, GOOGLE_MAVEN_URL)
}

/** Cronet-backed upstream used by the forced HTTP/3 feature of the TLS-MITM bridge. */
interface CronetStack : FeatureStack {

    /** True when an engine is live or the native library is available to build one. */
    fun isReady(): Boolean

    /** Warms the engine; kicks a one-shot background native download when missing. */
    fun start(echUpstream: Boolean)

    fun stop()

    /**
     * Forwards one HTTP/1.1 request through Cronet, writing a chunked HTTP/1.1
     * response to [sink]. Mirrors the pre-stack CronetForwarder contract.
     */
    fun forward(
        method: String,
        url: String,
        headers: List<Pair<String, String>>,
        body: ByteArray?,
        sink: OutputStream,
        clientWantsClose: Boolean,
        hostForHint: String,
        portForHint: Int
    ): Boolean
}
