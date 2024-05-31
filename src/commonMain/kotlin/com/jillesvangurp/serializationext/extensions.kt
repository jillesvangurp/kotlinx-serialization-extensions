@file:Suppress("unused")

package com.jillesvangurp.serializationext

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.double

fun JsonObject?.getObject(name: String): JsonObject? =
    this?.get(name)?.let { if (it is JsonNull) null else it as JsonObject }

fun JsonObject?.getDouble(name: String): Double? =
    this?.get(name)?.let {
        if (it is JsonNull) null
        else {
            it as JsonPrimitive
            it.double
        }
    }

fun JsonObject?.getArray(name: String): JsonArray? =
    this?.get(name)?.let {
        if (it is JsonNull) null
        else {
            it as JsonArray
        }
    }

fun JsonObject?.getStringArray(name: String): List<String> =
    this?.getArray(name)?.let { array ->
        array.map { e ->
            e as JsonPrimitive
            e.content
        }
    } ?: listOf()

fun JsonObject?.getDoubleArray(name: String): List<Double> =
    this?.get(name)?.let {
        it as JsonArray
        it.map { e ->
            e as JsonPrimitive
            e.double
        }
    } ?: listOf()

fun JsonObject?.getString(name: String): String? =
    this?.get(name)?.let {
        if (it is JsonNull) null
        else {
            it as JsonPrimitive
            it.content
        }
    }

fun JsonObject?.getString(enumValue: Enum<*>): String? = getString(enumValue.name)

fun JsonObject?.set(vararg pairs: Pair<String, JsonElement>): JsonObject? {
    if (this == null) return null
    return pairs.fold(this.toMap()) { map, pair -> map + pair }.let { JsonObject(it) }
}

fun JsonObject?.deleteKeys(vararg keys: String): JsonObject? {
    if (this == null) return null
    return JsonObject((this.toMap() - keys.toSet()))
}

fun List<*>.toJsonElement(): JsonArray {
    val list: MutableList<JsonElement> = mutableListOf()
    this.forEach {
        val value = it ?: return@forEach
        when (value) {
            is Map<*, *> -> list.add((value).toJsonElement())
            is List<*> -> list.add(value.toJsonElement())
            else -> list.add(JsonPrimitive(value.toString()))
        }
    }
    return JsonArray(list)
}

fun Map<*, *>.toJsonElement(): JsonObject {
    val map: MutableMap<String, JsonElement> = mutableMapOf()
    this.forEach {
        val key = it.key as? String ?: return@forEach
        val value = it.value ?: return@forEach
        when (value) {
            is Map<*, *> -> map[key] = (value).toJsonElement()
            is List<*> -> map[key] = value.toJsonElement()
            else -> map[key] = JsonPrimitive(value.toString())
        }
    }
    return JsonObject(map)
}

