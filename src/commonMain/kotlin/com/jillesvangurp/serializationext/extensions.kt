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

/** Backwards-compatible helpers if you want the old names */
fun List<*>.toJsonElement(): JsonArray = this.toJsonArray()
fun Map<*, *>.toJsonElement(): JsonObject = this.toJsonObject()

// FIXME add a version of foo["bar"] = e that calls toJsonElement on e where e:Any
// FIXME do the same for JsonArray.add()

/** Generic: turn anything into a JsonElement */
@Suppress("UNCHECKED_CAST")
fun Any?.toJsonElement(): JsonElement? = when (this) {
    null -> null
    is JsonElement -> this

    // Scalars
    is String -> JsonPrimitive(this)
    is Char -> JsonPrimitive(toString())
    is Boolean -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this.toString())
    is Enum<*> -> JsonPrimitive(name)

    // Maps
    is Map<*, *> -> toJsonObject()

    // Iterables / Sequences
    is Iterable<*> -> toJsonArray()
    is Sequence<*> -> toList().toJsonArray()
    is Array<*> -> asList().toJsonArray()

    // Primitive arrays
    is IntArray -> JsonArray(map { JsonPrimitive(it) })
    is LongArray -> JsonArray(map { JsonPrimitive(it) })
    is ShortArray -> JsonArray(map { JsonPrimitive(it) })
    is ByteArray -> JsonArray(map { JsonPrimitive(it) })
    is DoubleArray -> JsonArray(map { JsonPrimitive(it) })
    is FloatArray -> JsonArray(map { JsonPrimitive(it) })
    is BooleanArray -> JsonArray(map { JsonPrimitive(it) })
    is CharArray -> JsonArray(map { JsonPrimitive(it.toString()) })

    // Just use toString for anything else
    else -> JsonPrimitive(toString())
}

/** Iterable -> JsonArray */
fun Iterable<*>.toJsonArray(): JsonArray =
    JsonArray(mapNotNull { it.toJsonElement() })

/** Map -> JsonObject (stringify non-string keys) */
fun Map<*, *>.toJsonObject(): JsonObject {
    val out = buildMap<String, JsonElement> {
        for ((k, v) in this@toJsonObject) {
            if (v == null) continue
            val key = k?.toString() ?: continue

            v.toJsonElement()?.let {
                put(key, it)
            }
        }
    }
    return JsonObject(out)
}

