/**
 * Minimal MCP (Model Context Protocol) wire types used by [dev.anyfn.bridge.McpServer].
 *
 * This is not a full MCP implementation — only the messages anyfn needs to
 * publish a tool catalog and dispatch calls. Reference:
 * https://modelcontextprotocol.io/specification
 */
package dev.anyfn.core.protocol

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class McpRequest(
    val jsonrpc: String = "2.0",
    val id: JsonElement? = null,
    val method: String,
    val params: JsonObject? = null,
)

@Serializable
data class McpResponse(
    val jsonrpc: String = "2.0",
    val id: JsonElement? = null,
    val result: JsonElement? = null,
    val error: McpError? = null,
)

@Serializable
data class McpError(
    val code: Int,
    val message: String,
    val data: JsonElement? = null,
)

@Serializable
data class McpTool(
    val name: String,
    val description: String,
    val inputSchema: JsonObject,
)

@Serializable
data class McpToolListResult(val tools: List<McpTool>)

@Serializable
data class McpToolCallParams(
    val name: String,
    val arguments: JsonObject = JsonObject(emptyMap()),
)

@Serializable
data class McpToolCallResult(
    val content: List<McpContent>,
    val isError: Boolean = false,
)

@Serializable
sealed interface McpContent {
    @Serializable
    @SerialName("text")
    data class Text(val text: String) : McpContent

    @Serializable
    @SerialName("image")
    data class Image(val data: String, val mimeType: String) : McpContent
}

@Serializable
data class McpInitializeResult(
    val protocolVersion: String = "2024-11-05",
    val capabilities: McpCapabilities = McpCapabilities(),
    val serverInfo: McpServerInfo = McpServerInfo(),
)

@Serializable
data class McpCapabilities(
    val tools: McpToolsCapability = McpToolsCapability(),
)

@Serializable
data class McpToolsCapability(val listChanged: Boolean = true)

@Serializable
data class McpServerInfo(
    val name: String = "anyfn",
    val version: String = "0.1.0",
)

object McpErrors {
    const val PARSE_ERROR: Int = -32700
    const val INVALID_REQUEST: Int = -32600
    const val METHOD_NOT_FOUND: Int = -32601
    const val INVALID_PARAMS: Int = -32602
    const val INTERNAL_ERROR: Int = -32603
    const val TOOL_NOT_FOUND: Int = -32000
    const val INVOCATION_FAILED: Int = -32001
    const val NEEDS_CONFIRMATION: Int = -32002
}
