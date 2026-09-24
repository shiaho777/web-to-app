package com.webtoapp.core.share

/**
 * Shared constants and the wire format for the inbound share channel (issue #943).
 *
 * This is the counterpart of `core/webview/ShareBridge.kt`, which handles the *outbound*
 * direction (the page's own `navigator.share` → the system sheet). Here content travels the
 * other way: another app → the Android share sheet → `ACTION_SEND` on `ShellActivity` → the page.
 *
 * Everything in this package is shell-synced (`shell/build.gradle.kts` →
 * `syncShellRuntimeSources`), so it must stay free of host-only dependencies (no
 * `core/apkbuilder`, no `androidx.webkit`, no new third-party libraries).
 */
object ShareReceiveContract {

    /** The [MIME_IMAGES] filter declared in the exported APK's `ACTION_SEND` intent-filter. */
    const val MIME_IMAGES = "image/*"

    /** The `text/plain` filter, for link/text shares. Opt-in per app. */
    const val MIME_TEXT = "text/plain"

    /** Sub-directory of `cacheDir` holding received payloads plus the queue index. */
    const val INBOX_DIR_NAME = "shared_inbox"

    /** Queue index filename inside [INBOX_DIR_NAME]. */
    const val INDEX_FILE_NAME = "index.json"

    /**
     * Hard per-file ceiling for a persisted payload. A sender is an untrusted third party,
     * so the inbox must not be fillable by one share — anything above this is dropped.
     */
    const val MAX_FILE_BYTES: Long = 64L * 1024 * 1024

    /** Ceiling for the whole inbox; oldest items are evicted first. */
    const val MAX_TOTAL_BYTES: Long = 192L * 1024 * 1024

    /**
     * Ceiling on `ACTION_SEND_MULTIPLE` fan-out. A malicious sender can put an arbitrary
     * number of stream URIs in one intent.
     */
    const val MAX_ITEMS: Int = 10

    /**
     * Inline ceiling for the base64 data URL handed to the page over the JS channel.
     * Beyond this the item is still persisted and still offered to the file chooser, but
     * the page only receives metadata (`inline: false`) — shipping a multi-megabyte string
     * through `evaluateJavascript` is neither reliable nor kind to memory.
     */
    const val INLINE_MAX_BYTES: Long = 5L * 1024 * 1024

    /** Ceiling for a `text/plain` payload. */
    const val MAX_TEXT_CHARS: Int = 100_000

    /**
     * Items older than this are pruned. The queue is a hand-off buffer, not storage — an hour
     * comfortably covers "share it, then use it in the app", while keeping both the on-disk
     * footprint and the window in which the page is re-notified bounded.
     */
    const val TTL_MS: Long = 60L * 60 * 1000

    /** `kind` discriminator for a persisted image/video entry. */
    const val KIND_FILE = "file"

    /** `kind` discriminator for a `text/plain` entry. */
    const val KIND_TEXT = "text"

    /**
     * `ACTION_VIEW` "open with" registration (opt-in, `WebViewConfig.openWithEnabled`):
     * mime types declared on the mime-based intent-filter. The `text` wildcard covers
     * plain text and most code/config payloads whose sender bothers to label them; the
     * `application` entries catch the structured formats file managers tag explicitly.
     * `octet-stream` is deliberately absent — it would make the app a candidate for
     * *every* file.
     */
    val OPEN_WITH_MIME_TYPES = listOf(
        "text/*",
        "application/json",
        "application/xml",
        "application/x-yaml",
        "application/toml",
        "application/javascript",
        "application/x-javascript"
    )

    /**
     * Extension list for the `pathPattern`-based filter — many senders label text/code
     * files `application/octet-stream` or nothing at all, so suffix matching catches what
     * the mime filter cannot. Each entry becomes `.*\.<ext>` under both the `file` and
     * `content` schemes.
     */
    val OPEN_WITH_EXTENSIONS = listOf(
        "txt", "md", "markdown", "log", "csv", "tsv",
        "json", "json5", "xml", "yml", "yaml", "toml", "ini", "cfg", "conf",
        "properties", "prop",
        "js", "mjs", "cjs", "ts", "css",
        "py", "java", "kt", "kts",
        "c", "h", "cpp", "cc", "cxx", "hpp",
        "sh", "bash", "gradle", "go", "rs"
    )

    /** Name of the page-facing JS namespace installed at document-start. */
    const val JS_NAMESPACE = "WTAShareInbox"

    /** DOM event dispatched on `window` and `document` for every delivered batch. */
    const val JS_EVENT_NAME = "wta:share"

    /**
     * Name of the `addJavascriptInterface` object the bootstrap calls to mark items
     * consumed natively (`take()` / `consume(ids)`, issue #1038). Internal plumbing —
     * pages only ever see the [JS_NAMESPACE] API.
     */
    const val JS_BRIDGE_NAME = "__WTAShareInboxBridge"

    /** Default file name when a sender supplies none and none can be derived. */
    const val FALLBACK_NAME = "shared_content"
}

/**
 * One received payload, as persisted in the inbox index and mirrored to the page.
 *
 * [path] is only the on-disk location of the persisted copy; it is deliberately absent from
 * the JSON pushed to the page (the page gets a `file://` URL through
 * [ShareReceiveContract.JS_NAMESPACE] instead).
 */
data class SharedItem(
    val id: String,
    val kind: String,
    val mimeType: String,
    val name: String,
    val size: Long,
    val path: String? = null,
    val text: String? = null,
    val receivedAt: Long = System.currentTimeMillis(),
    /**
     * Non-zero once the page has consumed the item (issue #1038). Consumed items leave
     * the pending queues — JS re-announce and file-chooser prefill — while their payload
     * stays on disk until TTL, so a `fileUrl` already handed out keeps resolving.
     */
    val consumedAt: Long = 0
) {
    val isText: Boolean get() = kind == ShareReceiveContract.KIND_TEXT
    val isConsumed: Boolean get() = consumedAt > 0
}
