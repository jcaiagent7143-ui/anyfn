/**
 * Core domain types shared between the Android app and any future host
 * (desktop companion, MCP test rigs, etc.). Pure Kotlin, no Android deps.
 *
 * - [AppFunction] is the canonical description of a callable thing inside
 *   a target app. It carries enough metadata for an agent to call it
 *   (parameters) and enough for the Invoker to execute it (ui_path).
 * - [UiAction] is the union of replayable UI primitives.
 * - [ParameterSchema] is a minimal subset of JSON Schema sufficient to
 *   describe function arguments to an LLM.
 */
package dev.anyfn.core.model

import kotlinx.serialization.Serializable

@Serializable
data class AppFunction(
    val name: String,
    val packageName: String,
    val appLabel: String,
    val description: String,
    val parameters: List<ParameterSchema> = emptyList(),
    val uiPath: List<UiAction> = emptyList(),
    val confidence: Double = 0.0,
    val requiresReview: Boolean = false,
    val destructive: Boolean = false,
    val createdAtMillis: Long = 0L,
    val updatedAtMillis: Long = 0L,
) {
    init {
        require(name.matches(Regex("^[a-z][a-z0-9_]*$"))) {
            "function name must be snake_case: $name"
        }
        require(packageName.isNotBlank()) { "packageName required" }
    }
}

@Serializable
data class ParameterSchema(
    val name: String,
    val type: ParamType,
    val description: String,
    val required: Boolean = true,
    val enumValues: List<String>? = null,
    val defaultValue: String? = null,
)

@Serializable
enum class ParamType { STRING, INTEGER, NUMBER, BOOLEAN, OBJECT, ARRAY }

@Serializable
sealed interface UiAction {
    @Serializable
    data class Click(
        val selector: Selector,
        val fallbackSelector: Selector? = null,
    ) : UiAction

    @Serializable
    data class TypeText(
        val selector: Selector,
        val valueFromParam: String,
    ) : UiAction

    @Serializable
    data class Scroll(
        val direction: ScrollDirection,
        val until: Selector? = null,
        val maxSteps: Int = 10,
    ) : UiAction

    @Serializable
    data class WaitFor(
        val selector: Selector,
        val timeoutMs: Long = 5_000L,
    ) : UiAction

    @Serializable
    data object PressEnter : UiAction

    @Serializable
    data object PressBack : UiAction

    @Serializable
    data class Launch(val packageName: String) : UiAction
}

@Serializable
enum class ScrollDirection { UP, DOWN, LEFT, RIGHT }

@Serializable
data class Selector(
    val byResourceId: String? = null,
    val byText: String? = null,
    val byContentDescription: String? = null,
    val byClassName: String? = null,
) {
    init {
        require(
            byResourceId != null || byText != null ||
                byContentDescription != null || byClassName != null,
        ) { "Selector requires at least one matcher" }
    }
}
