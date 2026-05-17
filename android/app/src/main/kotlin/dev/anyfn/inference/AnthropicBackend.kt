/**
 * AnthropicBackend
 *
 * Sends the inference prompt to Anthropic's Messages API and parses the
 * returned JSON into [AppFunction]s. Uses Ktor's CIO client directly so we
 * don't take a hard dependency on a Java SDK that might not ship Android
 * artefacts cleanly.
 *
 * The model is pinned at `claude-sonnet-4-6` — the cheapest model that
 * reliably produces well-formed JSON of this shape. Override via
 * [DEFAULT_MODEL] if you need to.
 */
package dev.anyfn.inference

import dev.anyfn.core.model.AppFunction
import dev.anyfn.core.model.ParamType
import dev.anyfn.core.model.ParameterSchema
import dev.anyfn.core.model.ScrollDirection
import dev.anyfn.core.model.Selector
import dev.anyfn.core.model.UiAction
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.timeout
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AnthropicBackend(private val prompts: PromptLibrary) : LlmBackend {

    @Volatile private var apiKey: String = ""
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun setApiKey(value: String) {
        apiKey = value
    }

    override suspend fun isReady(): Boolean = apiKey.isNotBlank()

    override suspend fun inferFunctions(
        packageName: String,
        appLabel: String,
        uiTreeJson: String,
    ): Result<List<AppFunction>> = runCatching {
        require(apiKey.isNotBlank()) { "Anthropic API key not set" }

        val prompt = prompts.render(
            "infer_functions.md",
            mapOf(
                "package_name" to packageName,
                "app_label" to appLabel,
                "ui_tree_json" to uiTreeJson,
            ),
        )

        val response: HttpResponse = client.post(API_URL) {
            timeout { requestTimeoutMillis = 60_000 }
            header("x-api-key", apiKey)
            header("anthropic-version", "2023-06-01")
            contentType(ContentType.Application.Json)
            setBody(
                Request(
                    model = DEFAULT_MODEL,
                    maxTokens = 4096,
                    messages = listOf(Message(role = "user", content = prompt)),
                ),
            )
        }
        if (!response.status.isSuccess()) {
            error("anthropic api ${response.status.value}: ${response.body<String>()}")
        }
        val body: Response = response.body()
        val text = body.content.firstOrNull { it.type == "text" }?.text.orEmpty()
        parseFunctions(text, packageName, appLabel)
    }

    private fun parseFunctions(raw: String, packageName: String, appLabel: String): List<AppFunction> {
        val trimmed = raw.trim()
            .removePrefix("```json").removePrefix("```")
            .removeSuffix("```").trim()
        val arr: JsonArray = (json.parseToJsonElement(trimmed) as? JsonArray) ?: return emptyList()
        return arr.mapNotNull { el ->
            runCatching { decodeFunction(el.jsonObject, packageName, appLabel) }.getOrNull()
        }
    }

    private fun decodeFunction(obj: JsonObject, packageName: String, appLabel: String): AppFunction {
        val now = System.currentTimeMillis()
        return AppFunction(
            name = obj.getValue("name").jsonPrimitive.content,
            packageName = packageName,
            appLabel = appLabel,
            description = obj.getValue("description").jsonPrimitive.content,
            parameters = obj["parameters"]?.jsonArray?.map { decodeParam(it.jsonObject) }.orEmpty(),
            uiPath = obj["ui_path"]?.jsonArray?.mapNotNull { decodeAction(it.jsonObject) }.orEmpty(),
            confidence = obj["confidence"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.7,
            requiresReview = obj["requires_review"]?.jsonPrimitive?.content?.toBoolean() ?: false,
            destructive = obj["destructive"]?.jsonPrimitive?.content?.toBoolean() ?: false,
            createdAtMillis = now,
            updatedAtMillis = now,
        )
    }

    private fun decodeParam(obj: JsonObject): ParameterSchema = ParameterSchema(
        name = obj.getValue("name").jsonPrimitive.content,
        type = ParamType.valueOf(obj.getValue("type").jsonPrimitive.content.uppercase()),
        description = obj["description"]?.jsonPrimitive?.contentOrNull.orEmpty(),
        required = obj["required"]?.jsonPrimitive?.content?.toBoolean() ?: true,
        enumValues = obj["enum_values"]?.jsonArray?.map { it.jsonPrimitive.content },
        defaultValue = obj["default_value"]?.jsonPrimitive?.contentOrNull,
    )

    private fun decodeSelector(obj: JsonObject): Selector = Selector(
        byResourceId = obj["by_resource_id"]?.jsonPrimitive?.contentOrNull,
        byText = obj["by_text"]?.jsonPrimitive?.contentOrNull,
        byContentDescription = obj["by_content_description"]?.jsonPrimitive?.contentOrNull,
        byClassName = obj["by_class_name"]?.jsonPrimitive?.contentOrNull,
    )

    private fun decodeAction(obj: JsonObject): UiAction? {
        val type = obj["type"]?.jsonPrimitive?.contentOrNull ?: return null
        return when (type) {
            "Click" -> UiAction.Click(
                selector = decodeSelector(obj.getValue("selector").jsonObject),
                fallbackSelector = obj["fallback_selector"]?.jsonObject?.let { decodeSelector(it) },
            )
            "TypeText" -> UiAction.TypeText(
                selector = decodeSelector(obj.getValue("selector").jsonObject),
                valueFromParam = obj.getValue("value_from_param").jsonPrimitive.content,
            )
            "Scroll" -> UiAction.Scroll(
                direction = ScrollDirection.valueOf(
                    obj.getValue("direction").jsonPrimitive.content.uppercase(),
                ),
                until = obj["until"]?.jsonObject?.let { decodeSelector(it) },
                maxSteps = obj["max_steps"]?.jsonPrimitive?.content?.toIntOrNull() ?: 10,
            )
            "WaitFor" -> UiAction.WaitFor(
                selector = decodeSelector(obj.getValue("selector").jsonObject),
                timeoutMs = obj["timeout_ms"]?.jsonPrimitive?.content?.toLongOrNull() ?: 5_000L,
            )
            "PressEnter" -> UiAction.PressEnter
            "PressBack" -> UiAction.PressBack
            "Launch" -> UiAction.Launch(obj.getValue("package_name").jsonPrimitive.content)
            else -> null
        }
    }

    private val client: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    @Serializable
    private data class Request(
        val model: String,
        @SerialName("max_tokens") val maxTokens: Int,
        val messages: List<Message>,
    )

    @Serializable
    private data class Message(val role: String, val content: String)

    @Serializable
    private data class Response(val content: List<Content> = emptyList())

    @Serializable
    private data class Content(val type: String, val text: String? = null)

    companion object {
        const val DEFAULT_MODEL: String = "claude-sonnet-4-6"
        const val API_URL: String = "https://api.anthropic.com/v1/messages"
    }
}
