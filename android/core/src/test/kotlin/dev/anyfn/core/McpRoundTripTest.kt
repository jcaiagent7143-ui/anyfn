package dev.anyfn.core

import dev.anyfn.core.protocol.McpInitializeResult
import dev.anyfn.core.protocol.McpRequest
import dev.anyfn.core.protocol.McpResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class McpRoundTripTest {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @Test
    fun `initialize result encodes to expected shape`() {
        val encoded = json.encodeToString(McpInitializeResult.serializer(), McpInitializeResult())
        assertEquals(true, encoded.contains("\"protocolVersion\""))
        assertEquals(true, encoded.contains("\"serverInfo\""))
    }

    @Test
    fun `request decodes with numeric id`() {
        val raw = """{"jsonrpc":"2.0","id":42,"method":"tools/list"}"""
        val req = json.decodeFromString(McpRequest.serializer(), raw)
        assertEquals("tools/list", req.method)
        assertEquals(JsonPrimitive(42), req.id)
    }

    @Test
    fun `response can carry error`() {
        val r = McpResponse(error = dev.anyfn.core.protocol.McpError(code = -32601, message = "nope"))
        val encoded = json.encodeToString(McpResponse.serializer(), r)
        assertEquals(true, encoded.contains("-32601"))
    }
}
