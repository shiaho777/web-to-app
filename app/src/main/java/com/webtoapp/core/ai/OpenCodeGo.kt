package com.webtoapp.core.ai

import com.webtoapp.core.network.NetworkModule
import okhttp3.Call
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import java.util.UUID

internal object OpenCodeGo {
    private const val CONSOLE_PATH = "/inference/go"
    private const val LEGACY_PATH = "/zen/go"
    private const val SESSION_HEADER = "x-opencode-session"

    private val consoleRoutes = mapOf(
        "/v1/chat/completions" to "/openai/v1/chat/completions",
        "/v1/responses" to "/openai/v1/responses",
        "/v1/messages" to "/anthropic/v1/messages"
    )

    private data class SessionHeader(val value: String)

    private val client by lazy {
        NetworkModule.streamingClient.newBuilder()
            .addNetworkInterceptor { chain -> chain.proceed(applySessionHeader(chain.request())) }
            .build()
    }

    fun inferenceUrl(url: String): String {
        val parsed = url.toHttpUrlOrNull()?.takeIf(::isOfficialOrigin) ?: return url
        if (!parsed.encodedPath.startsWith("$CONSOLE_PATH/")) return url
        val route = consoleRoutes[parsed.encodedPath.trimEnd('/').removePrefix(CONSOLE_PATH)]
            ?: return url
        return parsed.newBuilder().encodedPath(CONSOLE_PATH + route).build().toString()
    }

    fun isInferenceUrl(url: String): Boolean =
        url.toHttpUrlOrNull()?.let(::isInferenceUrl) == true

    fun usesConsoleAuth(url: String): Boolean =
        url.toHttpUrlOrNull()?.let {
            isInferenceUrl(it) && it.encodedPath.startsWith("$CONSOLE_PATH/")
        } == true

    fun newCall(request: Request, sessionId: String): Call {
        if (!isInferenceRequest(request)) return NetworkModule.streamingClient.newCall(request)
        val id = UUID.nameUUIDFromBytes(sessionId.toByteArray(Charsets.UTF_8)).toString().replace("-", "")
        val tagged = request.newBuilder()
            .tag(SessionHeader::class.java, SessionHeader("ses_$id"))
            .build()
        return client.newCall(tagged)
    }

    internal fun applySessionHeader(request: Request): Request {
        val session = request.tag(SessionHeader::class.java) ?: return request
        val builder = request.newBuilder()
        if (isInferenceRequest(request)) builder.header(SESSION_HEADER, session.value)
        else builder.removeHeader(SESSION_HEADER)
        return builder.build()
    }

    private fun isInferenceRequest(request: Request): Boolean =
        request.method == "POST" && isInferenceUrl(request.url)

    private fun isInferenceUrl(url: HttpUrl): Boolean {
        if (!isOfficialOrigin(url)) return false
        val path = url.encodedPath.trimEnd('/')
        return when {
            path.startsWith("$CONSOLE_PATH/") -> path.removePrefix(CONSOLE_PATH) in consoleRoutes.values
            path.startsWith("$LEGACY_PATH/") -> path.removePrefix(LEGACY_PATH) in consoleRoutes.keys
            else -> false
        }
    }

    private fun isOfficialOrigin(url: HttpUrl): Boolean =
        url.isHttps && url.host == "opencode.ai" && url.port == 443
}
