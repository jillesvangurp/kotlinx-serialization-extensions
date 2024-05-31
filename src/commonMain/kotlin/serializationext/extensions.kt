@file:Suppress("unused")

package serializationext

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
