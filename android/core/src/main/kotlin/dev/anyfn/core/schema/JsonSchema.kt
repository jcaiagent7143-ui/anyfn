/**
 * Helpers for converting [dev.anyfn.core.model.ParameterSchema] lists into
 * the JSON-Schema object an MCP `tools/list` response expects, and for
 * converting an MCP `tools/call` arguments object back into a typed map.
 */
package dev.anyfn.core.schema

import dev.anyfn.core.model.ParamType
import dev.anyfn.core.model.ParameterSchema
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

object JsonSchema {

    fun forParameters(params: List<ParameterSchema>): JsonObject = buildJsonObject {
        put("type", JsonPrimitive("object"))
        put(
            "properties",
            buildJsonObject {
                params.forEach { p -> put(p.name, propertySchema(p)) }
            },
        )
        put(
            "required",
            JsonArray(params.filter { it.required }.map { JsonPrimitive(it.name) }),
        )
    }

    private fun propertySchema(p: ParameterSchema): JsonObject = buildJsonObject {
        put("type", JsonPrimitive(p.type.jsonName()))
        put("description", JsonPrimitive(p.description))
        p.enumValues?.let { values ->
            put("enum", buildJsonArray { values.forEach { add(JsonPrimitive(it)) } })
        }
        p.defaultValue?.let { put("default", JsonPrimitive(it)) }
    }

    private fun ParamType.jsonName(): String = when (this) {
        ParamType.STRING -> "string"
        ParamType.INTEGER -> "integer"
        ParamType.NUMBER -> "number"
        ParamType.BOOLEAN -> "boolean"
        ParamType.OBJECT -> "object"
        ParamType.ARRAY -> "array"
    }
}
