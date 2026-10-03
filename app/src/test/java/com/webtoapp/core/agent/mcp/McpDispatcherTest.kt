package com.webtoapp.core.agent.mcp

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.webtoapp.core.agent.tool.Tool
import com.webtoapp.core.agent.tool.ToolContext
import com.webtoapp.core.agent.tool.ToolResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class McpDispatcherTest {

    private class EchoTool : Tool {
        override val name = "echo"
        override val description = "Echo the text argument."
        override val parametersSchema: JsonElement = JsonObject().apply {
            addProperty("type", "object")
        }
        override fun isReadOnly() = true
        override suspend fun execute(args: JsonObject, ctx: ToolContext): ToolResult {
            return ToolResult.ok(args.get("text")?.asString ?: "")
        }
    }

    private fun dispatcher(result: ToolResult = ToolResult.ok("pong")) = McpDispatcher(
        tools = { listOf(EchoTool()) },
        callTool = { _, _ -> result }
    )

    @Test
    fun initializeAdvertisesToolsAndProtocol() = runBlocking {
        val message = dispatcher().handle("""{"jsonrpc":"2.0","id":1,"method":"initialize"}""")
        val json = (message as McpDispatcher.DispatchResult.Message).json
        val result = json.getAsJsonObject("result")
        assertEquals(McpDispatcher.PROTOCOL, result.get("protocolVersion").asString)
        assertTrue(result.getAsJsonObject("capabilities").has("tools"))
        assertEquals(1, json.get("id").asInt)
    }

    @Test
    fun initializedNotificationHasNoBody() = runBlocking {
        val message = dispatcher().handle(
            """{"jsonrpc":"2.0","method":"notifications/initialized"}"""
        )
        assertTrue(message is McpDispatcher.DispatchResult.Notification)
    }

    @Test
    fun toolsListUsesTheSameSchemaTheAgentAdvertises() = runBlocking {
        val message = dispatcher().handle("""{"jsonrpc":"2.0","id":2,"method":"tools/list"}""")
        val tools = (message as McpDispatcher.DispatchResult.Message).json
            .getAsJsonObject("result")
            .getAsJsonArray("tools")
        assertEquals(1, tools.size())
        val tool = tools[0].asJsonObject
        assertEquals("echo", tool.get("name").asString)
        assertEquals("Echo the text argument.", tool.get("description").asString)
        assertEquals("object", tool.getAsJsonObject("inputSchema").get("type").asString)
    }

    @Test
    fun toolsCallReturnsTextContent() = runBlocking {
        val message = dispatcher(ToolResult.ok("pong")).handle(
            """{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"echo","arguments":{"text":"hi"}}}"""
        )
        val result = (message as McpDispatcher.DispatchResult.Message).json.getAsJsonObject("result")
        assertFalse(result.get("isError").asBoolean)
        assertEquals(
            "pong",
            result.getAsJsonArray("content")[0].asJsonObject.get("text").asString
        )
    }

    @Test
    fun unknownToolIsAnErrorResultNotAProtocolFailure() = runBlocking {
        val message = dispatcher().handle(
            """{"jsonrpc":"2.0","id":4,"method":"tools/call","params":{"name":"missing","arguments":{}}}"""
        )
        val json = (message as McpDispatcher.DispatchResult.Message).json
        assertFalse(json.has("error"))
        assertTrue(json.getAsJsonObject("result").get("isError").asBoolean)
    }

    @Test
    fun garbageIsAParseError() = runBlocking {
        val message = dispatcher().handle("not-json")
        val error = (message as McpDispatcher.DispatchResult.Message).json.getAsJsonObject("error")
        assertEquals(-32700, error.get("code").asInt)
    }

    @Test
    fun validJsonThatIsNotAnObjectIsAnInvalidRequest() = runBlocking {
        val message = dispatcher().handle("1") as McpDispatcher.DispatchResult.Message
        assertEquals(-32600, message.json.getAsJsonObject("error").get("code").asInt)
    }

    @Test
    fun discoverAdvertisesTheCurrentProtocol() = runBlocking {
        val message = dispatcher().handle(
            """{"jsonrpc":"2.0","id":"d","method":"server/discover","params":{"_meta":{"io.modelcontextprotocol/protocolVersion":"2026-07-28"}}}""",
            headers = mapOf("mcp-protocol-version" to "2026-07-28")
        ) as McpDispatcher.DispatchResult.Message
        assertEquals(200, message.httpStatus)
        val result = message.json.getAsJsonObject("result")
        val versions = result.getAsJsonArray("supportedVersions").map { it.asString }
        assertTrue(versions.contains("2026-07-28"))
        assertEquals("complete", result.get("resultType").asString)
    }

    @Test
    fun unknownProtocolVersionNamesWhatWeSpeak() = runBlocking {
        val message = dispatcher().handle(
            """{"jsonrpc":"2.0","id":9,"method":"tools/list","params":{"_meta":{"io.modelcontextprotocol/protocolVersion":"1900-01-01"}}}""",
            headers = mapOf("mcp-protocol-version" to "1900-01-01")
        ) as McpDispatcher.DispatchResult.Message
        assertEquals(400, message.httpStatus)
        val error = message.json.getAsJsonObject("error")
        assertEquals(-32022, error.get("code").asInt)
        val supported = error.getAsJsonObject("data").getAsJsonArray("supported").map { it.asString }
        assertTrue(supported.contains("2026-07-28"))
        assertEquals("1900-01-01", error.getAsJsonObject("data").get("requested").asString)
    }

    @Test
    fun mismatchedProtocolHeadersAreRejected() = runBlocking {
        val message = dispatcher().handle(
            """{"jsonrpc":"2.0","id":1,"method":"ping","params":{"_meta":{"io.modelcontextprotocol/protocolVersion":"2026-07-28"}}}""",
            headers = mapOf("mcp-protocol-version" to "2025-03-26")
        ) as McpDispatcher.DispatchResult.Message
        assertEquals(400, message.httpStatus)
        assertEquals(-32020, message.json.getAsJsonObject("error").get("code").asInt)
    }

    @Test
    fun loopbackServerRejectsAMissingToken() {
        val server = LoopbackHttpServer(0) { request ->
            val header = request.headers["authorization"]
            if (header != "Bearer secret") {
                HttpResponse(401, "Unauthorized", "application/json", """{"error":"unauthorized"}""")
            } else {
                HttpResponse(200, "OK", "application/json", """{"ok":true}""")
            }
        }
        server.start()
        try {
            val denied = post(server.boundPort, "Bearer wrong", """{"ping":1}""")
            assertTrue(denied.startsWith("HTTP/1.1 401"))
            val allowed = post(server.boundPort, "Bearer secret", """{"ping":1}""")
            assertTrue(allowed.startsWith("HTTP/1.1 200"))
            assertTrue(allowed.contains(""""ok":true"""))
        } finally {
            server.stop()
        }
    }

    private fun post(port: Int, token: String, body: String): String {
        val payload = body.toByteArray()
        var last: Exception? = null
        repeat(20) {
            try {
                java.net.Socket("127.0.0.1", port).use { socket ->
                    val out = socket.getOutputStream()
                    val request = "POST /mcp HTTP/1.1\r\nHost: 127.0.0.1\r\nAuthorization: $token\r\nContent-Length: ${payload.size}\r\nConnection: close\r\n\r\n"
                    out.write(request.toByteArray())
                    out.write(payload)
                    out.flush()
                    return socket.getInputStream().readBytes().toString(Charsets.UTF_8)
                }
            } catch (e: Exception) {
                last = e
                Thread.sleep(25)
            }
        }
        throw last ?: IllegalStateException("no connection")
    }
}
