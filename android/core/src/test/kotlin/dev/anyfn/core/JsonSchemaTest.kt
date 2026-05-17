package dev.anyfn.core

import dev.anyfn.core.model.ParamType
import dev.anyfn.core.model.ParameterSchema
import dev.anyfn.core.schema.JsonSchema
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class JsonSchemaTest {

    @Test
    fun `empty params produces object schema with no required entries`() {
        val schema = JsonSchema.forParameters(emptyList())
        assertEquals(JsonPrimitive("object"), schema["type"])
        val required = schema["required"] as JsonArray
        assertTrue(required.isEmpty())
    }

    @Test
    fun `required and optional params are partitioned`() {
        val schema = JsonSchema.forParameters(
            listOf(
                ParameterSchema("query", ParamType.STRING, "search", required = true),
                ParameterSchema("limit", ParamType.INTEGER, "max results", required = false),
            ),
        )
        val required = (schema["required"] as JsonArray).map { (it as JsonPrimitive).content }
        assertEquals(listOf("query"), required)
        val properties = schema["properties"] as JsonObject
        assertEquals(JsonPrimitive("string"), (properties["query"] as JsonObject)["type"])
        assertEquals(JsonPrimitive("integer"), (properties["limit"] as JsonObject)["type"])
    }
}
