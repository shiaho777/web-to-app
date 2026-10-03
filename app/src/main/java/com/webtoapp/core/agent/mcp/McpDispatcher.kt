package com.webtoapp.core.agent.mcp

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.MalformedJsonException
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolResult
import java.io.StringReader
import java.math.BigDecimal

/**
 * MCP on the loopback port. Speaks the current stateless revision
 * (2026-07-28: `server/discover`, per-request version) and the legacy
 * handshake (`initialize`) so both generations of desktop agents can connect.
 * One JSON object in, one JSON object out. Notifications return
 * [DispatchResult.Notification] so HTTP can answer 202 with an empty body.
 */
class McpDispatcher(
    private val tools: () -> List<Tool>,
    private val callTool: suspend (name: String, args: JsonObject) -> ToolResult
) {
    suspend fun handle(body: String, headers: Map<String, String> = emptyMap()): DispatchResult {
        val element = try {
            parseStrict(body)
        } catch (_: Exception) {
            return message(error(null, PARSE_ERROR, "Parse error"), 400)
        }
        if (element.isJsonArray) {
            return message(error(null, INVALID_REQUEST, "JSON-RPC batches are not supported"), 400)
        }
        if (!element.isJsonObject) {
            return message(error(null, INVALID_REQUEST, "Invalid request"), 400)
        }
        return handleObject(element.asJsonObject, headers)
    }

    private suspend fun handleObject(request: JsonObject, headers: Map<String, String>): DispatchResult {
        val id = request.get("id")
        val notification = id == null || id.isJsonNull
        val method = request.get("method")?.takeIf { it.isJsonPrimitive }?.asString
        if (method.isNullOrBlank()) {
            return message(error(id, INVALID_REQUEST, "Missing method"), 400)
        }
        val params = request.get("params")?.takeIf { it.isJsonObject }?.asJsonObject ?: JsonObject()
        when (val version = resolveVersion(request, params, headers, method)) {
            is Version.Mismatch -> return message(error(id, HEADER_MISMATCH, "Header mismatch"), 400)
            is Version.Unsupported -> return message(unsupported(id, version.requested), 400)
            is Version.Ok -> {
                val modern = version.name == MODERN_PROTOCOL
                val result = when (method) {
                    "initialize" -> initializeResult(params)
                    "ping" -> JsonObject()
                    "server/discover" -> discoverResult()
                    "tools/list" -> toolsList(modern)
                    "tools/call" -> toolsCall(params)
                    "notifications/initialized", "initialized", "notifications/cancelled" -> {
                        return DispatchResult.Notification
                    }
                    else -> {
                        if (notification) return DispatchResult.Notification
                        val status = if (modern) 404 else 200
                        return message(error(id, METHOD_NOT_FOUND, "Method not found: $method"), status)
                    }
                }
                if (notification) return DispatchResult.Notification
                return message(ok(id, result), 200)
            }
        }
    }

    /**
     * Gson parses leniently, so `not-json` would become a string and look like
     * an invalid request. JSON-RPC wants a parse error for text that is not JSON.
     */
    private fun parseStrict(body: String): JsonElement {
        val reader = JsonReader(StringReader(body))
        reader.isLenient = false
        val element = readStrict(reader)
        if (reader.peek() != JsonToken.END_DOCUMENT) {
            throw MalformedJsonException("Trailing data")
        }
        return element
    }

    private fun readStrict(reader: JsonReader): JsonElement {
        return when (reader.peek()) {
            JsonToken.BEGIN_OBJECT -> {
                val obj = JsonObject()
                reader.beginObject()
                while (reader.hasNext()) {
                    obj.add(reader.nextName(), readStrict(reader))
                }
                reader.endObject()
                obj
            }
            JsonToken.BEGIN_ARRAY -> {
                val array = JsonArray()
                reader.beginArray()
                while (reader.hasNext()) array.add(readStrict(reader))
                reader.endArray()
                array
            }
            JsonToken.STRING -> JsonPrimitive(reader.nextString())
            JsonToken.NUMBER -> readNumber(reader.nextString())
            JsonToken.BOOLEAN -> JsonPrimitive(reader.nextBoolean())
            JsonToken.NULL -> {
                reader.nextNull()
                JsonNull.INSTANCE
            }
            else -> throw MalformedJsonException("Expected a JSON value")
        }
    }

    private fun readNumber(literal: String): JsonPrimitive {
        if (literal.any { it == '.' || it == 'e' || it == 'E' }) {
            return JsonPrimitive(BigDecimal(literal))
        }
        val asLong = literal.toLongOrNull() ?: return JsonPrimitive(BigDecimal(literal))
        return if (asLong in Int.MIN_VALUE..Int.MAX_VALUE.toLong()) {
            JsonPrimitive(asLong.toInt())
        } else {
            JsonPrimitive(asLong)
        }
    }

    private fun resolveVersion(
        request: JsonObject,
        params: JsonObject,
        headers: Map<String, String>,
        method: String
    ): Version {
        val headerVersion = headers["mcp-protocol-version"]?.trim()?.takeIf { it.isNotEmpty() }
        val meta = params.getAsJsonObjectOrNull("_meta") ?: request.getAsJsonObjectOrNull("_meta")
        val bodyVersion = meta?.get("io.modelcontextprotocol/protocolVersion")
            ?.takeIf { it.isJsonPrimitive }?.asString
        if (headerVersion != null && bodyVersion != null && headerVersion != bodyVersion) {
            return Version.Mismatch
        }
        val mirroredMethod = headers["mcp-method"]?.trim()?.takeIf { it.isNotEmpty() }
        if (mirroredMethod != null && mirroredMethod != method) return Version.Mismatch
        if (method == "tools/call") {
            val mirroredName = headers["mcp-name"]?.trim()?.takeIf { it.isNotEmpty() }
            val bodyName = params.get("name")?.takeIf { it.isJsonPrimitive }?.asString
            if (mirroredName != null && bodyName != null && mirroredName != bodyName) {
                return Version.Mismatch
            }
        }
        // A missing header is the pre-2025-06-18 client. Treat it as the legacy revision.
        val requested = headerVersion ?: bodyVersion ?: return Version.Ok(LEGACY_PROTOCOL)
        if (requested !in SUPPORTED) return Version.Unsupported(requested)
        return Version.Ok(requested)
    }

    private fun initializeResult(params: JsonObject): JsonObject {
        val requested = params.get("protocolVersion")?.takeIf { it.isJsonPrimitive }?.asString
        val version = if (requested != null && requested in LEGACY_PROTOCOLS) requested else LEGACY_PROTOCOL
        return JsonObject().apply {
            addProperty("protocolVersion", version)
            add("capabilities", JsonObject().apply {
                add("tools", JsonObject().apply { addProperty("listChanged", false) })
            })
            add("serverInfo", serverInfo())
            addProperty("instructions", INSTRUCTIONS)
        }
    }

    private fun discoverResult(): JsonObject = JsonObject().apply {
        addProperty("resultType", "complete")
        add("supportedVersions", JsonArray().apply { SUPPORTED.forEach { add(it) } })
        add("capabilities", JsonObject().apply { add("tools", JsonObject()) })
        add("_meta", JsonObject().apply {
            add("io.modelcontextprotocol/serverInfo", serverInfo())
        })
        addProperty("instructions", INSTRUCTIONS)
        addProperty("ttlMs", 3_600_000)
        addProperty("cacheScope", "public")
    }

    private fun serverInfo(): JsonObject = JsonObject().apply {
        addProperty("name", "webtoapp")
        addProperty("version", "1")
    }

    private fun toolsList(modern: Boolean): JsonObject = JsonObject().apply {
        if (modern) addProperty("resultType", "complete")
        add("tools", JsonArray().apply {
            tools().forEach { tool ->
                add(JsonObject().apply {
                    addProperty("name", tool.name)
                    addProperty("description", tool.description)
                    add("inputSchema", tool.parametersSchema)
                })
            }
        })
    }

    private suspend fun toolsCall(params: JsonObject): JsonObject {
        val name = params.get("name")?.takeIf { it.isJsonPrimitive }?.asString
            ?: return toolError("tools/call requires name")
        val args = params.get("arguments")?.takeIf { it.isJsonObject }?.asJsonObject ?: JsonObject()
        val tool = tools().firstOrNull { it.name == name }
            ?: return toolError("Unknown tool: $name")
        val outcome = try {
            callTool(name, args)
        } catch (e: Exception) {
            ToolResult.error("${tool.name} failed: ${e.message ?: e.javaClass.simpleName}")
        }
        return JsonObject().apply {
            add("content", JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("type", "text")
                    addProperty("text", outcome.text)
                })
            })
            addProperty("isError", outcome.isError)
        }
    }

    private fun ok(id: JsonElement?, result: JsonObject): JsonObject = JsonObject().apply {
        addProperty("jsonrpc", "2.0")
        if (id != null && !id.isJsonNull) add("id", id)
        add("result", result)
    }

    private fun error(id: JsonElement?, code: Int, message: String, data: JsonObject? = null): JsonObject =
        JsonObject().apply {
            addProperty("jsonrpc", "2.0")
            if (id != null && !id.isJsonNull) add("id", id)
            add("error", JsonObject().apply {
                addProperty("code", code)
                addProperty("message", message)
                if (data != null) add("data", data)
            })
        }

    private fun unsupported(id: JsonElement?, requested: String): JsonObject {
        val data = JsonObject().apply {
            add("supported", JsonArray().apply { SUPPORTED.forEach { add(it) } })
            addProperty("requested", requested)
        }
        return error(id, UNSUPPORTED_VERSION, "Unsupported protocol version", data)
    }

    private fun toolError(message: String): JsonObject = JsonObject().apply {
        add("content", JsonArray().apply {
            add(JsonObject().apply {
                addProperty("type", "text")
                addProperty("text", message)
            })
        })
        addProperty("isError", true)
    }

    private fun JsonObject.getAsJsonObjectOrNull(name: String): JsonObject? =
        get(name)?.takeIf { it.isJsonObject }?.asJsonObject

    private fun message(json: JsonObject, httpStatus: Int) = DispatchResult.Message(json, httpStatus)

    sealed class DispatchResult {
        data class Message(val json: JsonObject, val httpStatus: Int = 200) : DispatchResult()
        object Notification : DispatchResult()
    }

    private sealed class Version {
        data class Ok(val name: String) : Version()
        data class Unsupported(val requested: String) : Version()
        object Mismatch : Version()
    }

    companion object {
        /** Legacy handshake revision advertised when the client does not name one. */
        const val PROTOCOL = "2025-03-26"
        private const val LEGACY_PROTOCOL = PROTOCOL
        private const val MODERN_PROTOCOL = "2026-07-28"
        private val LEGACY_PROTOCOLS = setOf("2025-11-25", "2025-06-18", "2025-03-26")
        private val SUPPORTED = listOf(MODERN_PROTOCOL, "2025-11-25", "2025-06-18", LEGACY_PROTOCOL)
        private const val PARSE_ERROR = -32700
        private const val INVALID_REQUEST = -32600
        private const val METHOD_NOT_FOUND = -32601
        private const val HEADER_MISMATCH = -32020
        private const val UNSUPPORTED_VERSION = -32022
        private const val INSTRUCTIONS =
            "WebToApp on this phone. preview opens a saved app (appId) or an http(s) url in its own visible recents task, then reads or changes that page. Other tools match the in-app agent. A write asks the person on the phone before it runs."
    }
}
