package com.webtoapp.core.aicoding.tool.builtin

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

internal class JsonSchemaBuilder {
    private val properties = JsonObject()
    private val required = mutableListOf<String>()

    fun string(name: String, description: String, required: Boolean = false) {
        properties.add(name, JsonObject().apply {
            addProperty("type", "string")
            addProperty("description", description)
        })
        if (required) this.required += name
    }

    fun integer(name: String, description: String, default: Int? = null, required: Boolean = false) {
        properties.add(name, JsonObject().apply {
            addProperty("type", "integer")
            addProperty("description", description)
            if (default != null) addProperty("default", default)
        })
        if (required) this.required += name
    }

    fun boolean(name: String, description: String, default: Boolean? = null, required: Boolean = false) {
        properties.add(name, JsonObject().apply {
            addProperty("type", "boolean")
            addProperty("description", description)
            if (default != null) addProperty("default", default)
        })
        if (required) this.required += name
    }

    fun enum(name: String, values: List<String>, description: String, required: Boolean = false) {
        properties.add(name, JsonObject().apply {
            addProperty("type", "string")
            addProperty("description", description)
            add("enum", JsonArray().apply { values.forEach { add(it) } })
        })
        if (required) this.required += name
    }

    fun array(name: String, itemsSchema: JsonObject, description: String, minItems: Int? = null, maxItems: Int? = null, required: Boolean = false) {
        properties.add(name, JsonObject().apply {
            addProperty("type", "array")
            addProperty("description", description)
            add("items", itemsSchema)
            if (minItems != null) addProperty("minItems", minItems)
            if (maxItems != null) addProperty("maxItems", maxItems)
        })
        if (required) this.required += name
    }

    fun build(): JsonElement = JsonObject().apply {
        addProperty("type", "object")
        add("properties", properties)
        if (this@JsonSchemaBuilder.required.isNotEmpty()) {
            add("required", JsonArray().apply {
                this@JsonSchemaBuilder.required.forEach { add(it) }
            })
        }
    }
}

internal fun jsonSchema(init: JsonSchemaBuilder.() -> Unit): JsonElement =
    JsonSchemaBuilder().apply(init).build()
