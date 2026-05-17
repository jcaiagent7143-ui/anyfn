package dev.anyfn.core

import dev.anyfn.core.model.AppFunction
import dev.anyfn.core.model.ParamType
import dev.anyfn.core.model.ParameterSchema
import dev.anyfn.core.model.ScrollDirection
import dev.anyfn.core.model.Selector
import dev.anyfn.core.model.UiAction
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class AppFunctionTest {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; classDiscriminator = "type" }

    @Test
    fun `function names must be snake_case`() {
        assertThrows(IllegalArgumentException::class.java) {
            AppFunction(name = "TikTokSearch", packageName = "com.x", appLabel = "X", description = "x")
        }
    }

    @Test
    fun `function round-trips through json`() {
        val original = AppFunction(
            name = "tiktok_search",
            packageName = "com.zhiliaoapp.musically",
            appLabel = "TikTok",
            description = "Search TikTok for videos matching the query.",
            parameters = listOf(
                ParameterSchema(name = "query", type = ParamType.STRING, description = "Search terms"),
            ),
            uiPath = listOf(
                UiAction.Click(Selector(byContentDescription = "Search")),
                UiAction.TypeText(Selector(byResourceId = "com.zhiliaoapp.musically:id/search_field"), valueFromParam = "query"),
                UiAction.PressEnter,
                UiAction.Scroll(direction = ScrollDirection.DOWN),
            ),
            confidence = 0.85,
        )
        val encoded = json.encodeToString(AppFunction.serializer(), original)
        val decoded = json.decodeFromString(AppFunction.serializer(), encoded)
        assertEquals(original.name, decoded.name)
        assertEquals(original.uiPath.size, decoded.uiPath.size)
        assertEquals(original.parameters, decoded.parameters)
    }

    @Test
    fun `selector requires at least one matcher`() {
        assertThrows(IllegalArgumentException::class.java) { Selector() }
    }
}
