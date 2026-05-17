/**
 * McpServer
 *
 * Embedded Ktor server that speaks a minimal subset of the Model Context
 * Protocol. Exposes the function registry as MCP tools and dispatches
 * `tools/call` invocations to [ActionExecutor].
 *
 * Transports:
 *   - `/ws` — WebSocket, the primary transport.
 *   - `/sse` — Server-Sent Events fallback.
 *   - `POST /rpc` — single-shot JSON-RPC, used by [AdbBridge]'s stdio mode.
 *
 * Auth: optional shared secret read from settings. Bound to 127.0.0.1 unless
 * LAN mode is explicitly enabled.
 */
package dev.anyfn.bridge

import dev.anyfn.core.model.AppFunction
import dev.anyfn.core.model.FailureReason
import dev.anyfn.core.model.InvocationResult
import dev.anyfn.core.protocol.McpContent
import dev.anyfn.core.protocol.McpError
import dev.anyfn.core.protocol.McpErrors
import dev.anyfn.core.protocol.McpInitializeResult
import dev.anyfn.core.protocol.McpRequest
import dev.anyfn.core.protocol.McpResponse
import dev.anyfn.core.protocol.McpTool
import dev.anyfn.core.protocol.McpToolCallResult
import dev.anyfn.core.protocol.McpToolListResult
import dev.anyfn.core.schema.JsonSchema
import dev.anyfn.data.preferences.SettingsPreferences
import dev.anyfn.data.repository.FunctionRepository
import dev.anyfn.invoker.ActionExecutor
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.header
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Singleton
class McpServer @Inject constructor(
    private val repository: FunctionRepository,
    private val executor: ActionExecutor,
    private val settings: SettingsPreferences,
) {

    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private var engine: ApplicationEngine? = null

    @Volatile var lastError: String? = null
        private set

    fun isRunning(): Boolean = engine != null

    suspend fun start(): Result<Endpoint> = runCatching {
        if (engine != null) return@runCatching describe()
        val snap = settings.snapshot.first()
        val host = if (snap.mcpLanMode) "0.0.0.0" else "127.0.0.1"
        val port = snap.mcpPort
        val secret = snap.mcpSharedSecret.takeIf { it.isNotBlank() }

        engine = embeddedServer(CIO, host = host, port = port) {
            install(WebSockets) { pingPeriodMillis = 20_000L }
            install(ContentNegotiation) { json(json) }
            install(StatusPages) {
                exception<Throwable> { call, cause ->
                    lastError = cause.message
                    call.respondText(
                        text = """{"jsonrpc":"2.0","error":{"code":-32603,"message":"${cause.message}"}}""",
                        status = HttpStatusCode.InternalServerError,
                    )
                }
            }
            routing {
                get("/") {
                    call.respondText(
                        """{"server":"anyfn","version":"0.1.0","transports":["ws","sse","rpc"]}""",
                        io.ktor.http.ContentType.Application.Json,
                    )
                }
                webSocket("/ws") {
                    if (!authorise(secret, call.request.headers["x-anyfn-secret"])) {
                        close(CloseReason(4401, "unauthorized"))
                        return@webSocket
                    }
                    incoming.consumeEach { frame ->
                        if (frame is Frame.Text) {
                            val reply = dispatch(frame.readText())
                            send(Frame.Text(reply))
                        }
                    }
                }
                post("/rpc") {
                    if (!authorise(secret, call.request.header("x-anyfn-secret"))) {
                        call.respond(HttpStatusCode.Unauthorized, "unauthorized")
                        return@post
                    }
                    val body = call.receiveText()
                    call.respondText(dispatch(body), io.ktor.http.ContentType.Application.Json)
                }
            }
        }
        engine?.start(wait = false)
        describe()
    }

    fun stop() {
        engine?.stop(gracePeriodMillis = 250L, timeoutMillis = 1_500L)
        engine = null
    }

    private fun describe(): Endpoint {
        val snap = kotlinx.coroutines.runBlocking { settings.snapshot.first() }
        return Endpoint(
            host = if (snap.mcpLanMode) "0.0.0.0" else "127.0.0.1",
            port = snap.mcpPort,
            requiresSecret = snap.mcpSharedSecret.isNotBlank(),
        )
    }

    private fun authorise(expected: String?, supplied: String?): Boolean {
        if (expected == null) return true
        return expected == supplied
    }

    suspend fun dispatch(body: String): String {
        val request = try {
            json.decodeFromString<McpRequest>(body)
        } catch (e: Exception) {
            return errorResponse(null, McpErrors.PARSE_ERROR, "invalid json: ${e.message}")
        }
        return when (request.method) {
            "initialize" -> okResponse(request.id, json.encodeToJsonElement(McpInitializeResult()))
            "tools/list" -> handleList(request)
            "tools/call" -> handleCall(request)
            else -> errorResponse(request.id, McpErrors.METHOD_NOT_FOUND, "unknown method '${request.method}'")
        }
    }

    private suspend fun handleList(request: McpRequest): String {
        val functions = repository.observeEnabled().first()
        val tools = functions.map { it.toMcpTool() }
        val result = McpToolListResult(tools)
        return okResponse(request.id, json.encodeToJsonElement(result))
    }

    private suspend fun handleCall(request: McpRequest): String {
        val params = request.params ?: return errorResponse(request.id, McpErrors.INVALID_PARAMS, "missing params")
        val name = (params["name"] as? JsonPrimitive)?.content
            ?: return errorResponse(request.id, McpErrors.INVALID_PARAMS, "missing tool name")
        val args = (params["arguments"] as? JsonObject)?.mapValues { (_, v) -> v.jsonPrimitive.content }.orEmpty()

        val result = executor.call(name, args)
        return when (result) {
            is InvocationResult.Success -> {
                val content = McpToolCallResult(
                    content = listOf(McpContent.Text(result.output)),
                    isError = false,
                )
                okResponse(request.id, json.encodeToJsonElement(content))
            }
            is InvocationResult.Failure -> {
                val code = when (result.reason) {
                    FailureReason.ACCESSIBILITY_DENIED, FailureReason.PERMISSION_DENIED ->
                        McpErrors.INVOCATION_FAILED
                    else -> McpErrors.INVOCATION_FAILED
                }
                errorResponse(request.id, code, result.message)
            }
            is InvocationResult.NeedsConfirmation -> errorResponse(
                request.id,
                McpErrors.NEEDS_CONFIRMATION,
                result.prompt,
            )
        }
    }

    private fun AppFunction.toMcpTool(): McpTool = McpTool(
        name = name,
        description = description,
        inputSchema = JsonSchema.forParameters(parameters),
    )

    private fun okResponse(id: kotlinx.serialization.json.JsonElement?, result: kotlinx.serialization.json.JsonElement): String =
        json.encodeToString(McpResponse(id = id, result = result))

    private fun errorResponse(
        id: kotlinx.serialization.json.JsonElement?,
        code: Int,
        message: String,
    ): String = json.encodeToString(McpResponse(id = id, error = McpError(code = code, message = message)))

    private fun Json.encodeToJsonElement(value: Any): kotlinx.serialization.json.JsonElement = when (value) {
        is McpInitializeResult -> parseToJsonElement(encodeToString(value))
        is McpToolListResult -> parseToJsonElement(encodeToString(value))
        is McpToolCallResult -> parseToJsonElement(encodeToString(value))
        else -> parseToJsonElement(value.toString())
    }

    data class Endpoint(val host: String, val port: Int, val requiresSecret: Boolean) {
        fun wsUrl(): String = "ws://$host:$port/ws"
        fun rpcUrl(): String = "http://$host:$port/rpc"
    }
}
