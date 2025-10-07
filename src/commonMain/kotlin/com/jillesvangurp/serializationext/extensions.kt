@file:Suppress("unused")

package com.jillesvangurp.serializationext

import kotlin.reflect.KProperty
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

private fun JsonObject?.getNested(vararg names: String): JsonElement? {
    var current: JsonElement? = this
    for (name in names) {
        current = (current as? JsonObject)?.get(name)
        if (current == null || current is JsonNull) return null
    }
    return current
}

fun JsonObject?.getObject(vararg names: String): JsonObject? =
    getNested(*names) as? JsonObject

fun JsonObject?.getDouble(vararg names: String): Double? =
    (getNested(*names) as? JsonPrimitive)?.double

fun JsonObject?.getLong(vararg names: String): Long? =
    (getNested(*names) as? JsonPrimitive)?.longOrNull

fun JsonObject?.getBooleanOrNull(vararg names: String): Boolean? =
    (getNested(*names) as? JsonPrimitive)?.content?.toBooleanStrictOrNull()

fun JsonObject?.getBoolean(vararg names: String): Boolean = getBooleanOrNull(names = names) ?: false

fun JsonObject?.getArray(vararg names: String): JsonArray? =
    getNested(*names) as? JsonArray

fun JsonObject?.getStringArray(vararg names: String): List<String> =
    (getArray(*names)?.mapNotNull { (it as? JsonPrimitive)?.content }) ?: listOf()

fun JsonObject?.getDoubleArray(vararg names: String): List<Double> =
    (getArray(*names)?.mapNotNull { (it as? JsonPrimitive)?.double }) ?: listOf()

fun JsonObject?.getString(vararg names: String): String? =
    (getNested(*names) as? JsonPrimitive)?.content

fun JsonObject?.getString(enumValue: Enum<*>): String? = getString(enumValue.name)
fun JsonObject?.getString(property: KProperty<*>): String? = getString(property.name)

fun JsonObject?.deleteKeys(vararg keys: String): JsonObject? {
    if (this == null) return null
    return JsonObject((this.toMap() - keys.toSet()))
}

/** Backwards-compatible helpers if you want the old names */
fun List<*>.toJsonElement(): JsonArray = this.toJsonArray()
fun Map<*, *>.toJsonElement(): JsonObject = this.toJsonObject()

/** Enables foo.add(e) with automatic conversion */
fun JsonArray.add(value: Any?): JsonArray =
    JsonArray(this + listOfNotNull(value.toJsonElement()))

private fun deepCopy(element: JsonElement): JsonElement = when (element) {
    is JsonObject -> JsonObject(element.mapValues { deepCopy(it.value) })
    is JsonArray -> JsonArray(element.map { deepCopy(it) })
    else -> element
}

fun JsonElement.clone(): JsonElement = deepCopy(this)

fun JsonObject?.modify(builderAction: JsonObjectBuilder.() -> Unit): JsonObject {
    if (this == null) return buildJsonObject(builderAction)

    return buildJsonObject {
        for ((key, value) in this@modify) {
            put(key, deepCopy(value))
        }
        builderAction(this)
    }
}

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

