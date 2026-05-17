package dev.anyfn

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Sanity-checks the JSON shape the Anthropic backend expects to receive
 * from the model. If we ever change the prompt schema, this test will
 * remind us to update [AnthropicBackend.parseFunctions] in lockstep.
 */
class AnthropicParseTest {

    private val sample = """
        [
          {
            "name": "tiktok_search",
            "description": "Search TikTok for videos matching the query.",
            "parameters": [
              { "name": "query", "type": "string", "description": "Search terms", "required": true }
            ],
            "ui_path": [
              { "type": "Click", "selector": { "by_content_description": "Search" } },
              { "type": "TypeText", "selector": { "by_resource_id": "com.zhiliaoapp.musically:id/search_field" }, "value_from_param": "query" },
              { "type": "PressEnter" }
            ],
            "confidence": 0.86,
            "requires_review": false,
            "destructive": false
          }
        ]
    """.trimIndent()

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `model output is a JSON array of objects with expected keys`() {
        val parsed = json.parseToJsonElement(sample) as JsonArray
        assertEquals(1, parsed.size)
        val first = parsed[0].jsonObject
        assertEquals("tiktok_search", first["name"]?.jsonPrimitive?.content)
        assertEquals(3, first["ui_path"]?.jsonArray?.size)
    }
}
