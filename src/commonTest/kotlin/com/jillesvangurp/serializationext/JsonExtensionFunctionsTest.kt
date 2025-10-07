package com.jillesvangurp.serializationext

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put


class JsonElementExtensionsTest {
    // begin-testfunctions
    enum class SampleEnum { KEY }

    // Nested object lookup: returns the JsonObject when present, null when missing or not an object
    @Test
    fun getNestedObject_returnsObjectOrNull() {
        val jsonObject = buildJsonObject {
            put("nested", buildJsonObject { put("key", "value") })
        }
        jsonObject.getObject("nested") shouldBe JsonObject(mapOf("key" to JsonPrimitive("value")))
        jsonObject.getObject("nonexistent").shouldBeNull()
    }

    // Number lookup: unwraps JsonPrimitive double; returns null for explicit null or missing key
    @Test
    fun getDouble_handlesNullAndMissing() {
        val jsonObject = buildJsonObject {
            put("number", 42.0)
            put("nullValue", JsonNull) // explicit null should yield null
        }
        jsonObject.getDouble("number") shouldBe 42.0
        jsonObject.getDouble("nullValue").shouldBeNull()
        jsonObject.getDouble("nonexistent").shouldBeNull()
    }

    // Array lookup: returns array or null (including when the value is JsonNull)
    @Test
    fun getArray_returnsArrayOrNull() {
        val jsonObject = buildJsonObject {
            put("array", buildJsonArray { add("one"); add("two") })
            put("nullArray", JsonNull)
        }
        jsonObject.getArray("array") shouldBe JsonArray(
            listOf(
                JsonPrimitive("one"),
                JsonPrimitive("two"),
            ),
        )
        jsonObject.getArray("nullArray").shouldBeNull()
        jsonObject.getArray("nonexistent").shouldBeNull()
    }

    // Convenience extractor: returns a Kotlin List<String>, empty when key missing or not an array of strings
    @Test
    fun getStringArray_returnsListOrEmpty() {
        val jsonObject = buildJsonObject {
            put("stringArray", buildJsonArray { add("one"); add("two") })
        }
        jsonObject.getStringArray("stringArray") shouldBe listOf("one", "two")
        jsonObject.getStringArray("nonexistent") shouldBe emptyList()
    }

    // Convenience extractor: returns a Kotlin List<Double>, empty when missing
    @Test
    fun getDoubleArray_returnsListOrEmpty() {
        val jsonObject = buildJsonObject {
            put("doubleArray", buildJsonArray { add(1.0); add(2.0) })
        }
        jsonObject.getDoubleArray("doubleArray") shouldBe listOf(1.0, 2.0)
        jsonObject.getDoubleArray("nonexistent") shouldBe emptyList()
    }

    // String lookup: returns value or null (for explicit null or missing key)
    @Test
    fun getString_returnsValueOrNull() {
        val jsonObject = buildJsonObject {
            put("string", "value")
            put("nullValue", JsonNull)
        }
        jsonObject.getString("string") shouldBe "value"
        jsonObject.getString("nullValue").shouldBeNull()
        jsonObject.getString("nonexistent").shouldBeNull()
    }

    // Overloads: supports enum keys and raw string keys
    @Test
    fun getString_supportsEnumKeys() {
        val jsonObject = buildJsonObject {
            put("KEY", "value")
        }
        jsonObject.getString(SampleEnum.KEY) shouldBe "value"
        jsonObject.getString(SampleEnum.KEY.name) shouldBe "value"
    }

    // Non-destructive delete: returns a copy without the specified keys
    @Test
    fun deleteKeys_removesSpecifiedKeys() {
        val jsonObject = buildJsonObject {
            put("key1", "value1")
            put("key2", "value2")
        }
        val updatedJsonObject = jsonObject.deleteKeys("key1")
        updatedJsonObject shouldBe JsonObject(mapOf("key2" to JsonPrimitive("value2")))
    }

    // Generic List -> JsonElement (JsonArray) conversion: supports primitives, maps, and nested lists
    @Test
    fun list_toJsonElement_serializesSupportedTypes() {
        val list = listOf("one", 2, mapOf("key" to "value"), listOf(1, 2, 3))
        val jsonArray = list.toJsonElement()

        jsonArray shouldBe JsonArray(
            listOf(
                JsonPrimitive("one"),
                JsonPrimitive("2"), // numbers become strings by design in this helper
                JsonObject(mapOf("key" to JsonPrimitive("value"))),
                JsonArray(listOf(JsonPrimitive("1"), JsonPrimitive("2"), JsonPrimitive("3"))),
            ),
        )
    }

    // Generic Map -> JsonElement (JsonObject) conversion: supports primitives, maps, and lists
    @Test
    fun map_toJsonElement_serializesSupportedTypes() {
        val map = mapOf(
            "string" to "value",
            "number" to 42,
            "nestedMap" to mapOf("key" to "value"),
            "list" to listOf(1, 2, 3),
        )
        val jsonObject = map.toJsonElement()

        jsonObject shouldBe JsonObject(
            mapOf(
                "string" to JsonPrimitive("value"),
                "number" to JsonPrimitive("42"),
                "nestedMap" to JsonObject(mapOf("key" to JsonPrimitive("value"))),
                "list" to JsonArray(
                    listOf(
                        JsonPrimitive("1"),
                        JsonPrimitive("2"),
                        JsonPrimitive("3"),
                    ),
                ),
            ),
        )
    }

    // Edge case: empty list becomes an empty JsonArray
    @Test
    fun emptyList_toJsonElement_isEmptyArray() {
        val list = emptyList<Any?>()
        val jsonArray = list.toJsonElement()
        jsonArray shouldBe JsonArray(emptyList())
    }

    // Edge case: empty map becomes an empty JsonObject
    @Test
    fun emptyMap_toJsonElement_isEmptyObject() {
        val map = emptyMap<String, Any?>()
        val jsonObject = map.toJsonElement()
        jsonObject shouldBe JsonObject(emptyMap())
    }

    // Null handling: nulls are dropped from lists
    @Test
    fun list_toJsonElement_ignoresNulls() {
        val list = listOf(null, "test", null)
        val jsonArray = list.toJsonElement()
        jsonArray shouldBe JsonArray(listOf(JsonPrimitive("test")))
    }

    // Null handling: null-valued entries are dropped from maps
    @Test
    fun map_toJsonElement_ignoresNullValues() {
        val map = mapOf("key1" to null, "key2" to "value")
        val jsonObject = map.toJsonElement()
        jsonObject shouldBe JsonObject(mapOf("key2" to JsonPrimitive("value")))
    }

    @Test
    fun modify_jsonObject() {
        val obj = buildJsonObject {}
        val modified = obj.modify {
            put("foo", "bar")
        }
        modified.getString("foo") shouldBe "bar"
    }

    @Test
    fun nestedGet() {
        val obj = buildJsonObject {
            put(
                "foo",
                buildJsonObject {
                    put("meaningOfLife", 42)
                    put("bar", "bar")
                },
            )
        }
        obj.getString("foo", "meaningOfLife") shouldBe "42"
        obj.getLong("foo", "meaningOfLife") shouldBe 42
        obj.getDouble("foo", "meaningOfLife") shouldBe 42.0
        obj.getBoolean("foo", "meaningOfLife") shouldBe false
        obj.getBooleanOrNull("foo", "meaningOfLife") shouldBe null
        obj.getString("foo", "bar") shouldBe "bar"
    }


    @Test
    fun getLong_handlesLongValues() {
        val jsonObject = buildJsonObject { put("num", 1234567890123L) }
        jsonObject.getLong("num") shouldBe 1234567890123L
    }

    @Test
    fun getBooleanAndBooleanOrNull_behavior() {
        val jsonObject = buildJsonObject {
            put("trueVal", true)
            put("falseVal", false)
            put("stringTrue", "true")
            put("stringInvalid", "yes")
        }
        jsonObject.getBoolean("trueVal") shouldBe true
        jsonObject.getBoolean("falseVal") shouldBe false
        jsonObject.getBooleanOrNull("stringTrue") shouldBe true
        jsonObject.getBoolean("stringInvalid") shouldBe false
        jsonObject.getBooleanOrNull("nonexistent").shouldBeNull()
    }

    @Test
    fun add_convertsAndAppendsElements() {
        val array = JsonArray(listOf(JsonPrimitive("existing")))
        val updated = array.add(42)
        updated shouldBe JsonArray(listOf(JsonPrimitive("existing"), JsonPrimitive("42")))
    }

    @Test
    fun clone_performsDeepCopy() {
        val original = buildJsonObject {
            put("nested", buildJsonObject { put("key", "value") })
        }
        val clone = original.clone()
        clone shouldBe original
        (clone as JsonObject)["nested"] shouldBe JsonObject(mapOf("key" to JsonPrimitive("value")))
        // modify original should not affect clone
        (original["nested"] as JsonObject).toMutableMap()["key"] = JsonPrimitive("changed")
        clone shouldBe JsonObject(mapOf("nested" to JsonObject(mapOf("key" to JsonPrimitive("value")))))
    }

    @Test
    fun modify_preservesExistingAndAddsNew() {
        val obj = buildJsonObject { put("a", "b") }
        val modified = obj.modify { put("c", "d") }
        modified.getString("a") shouldBe "b"
        modified.getString("c") shouldBe "d"
    }

    @Test
    fun toJsonElement_convertsVariousTypes() {
        val cases = mapOf<Any?, JsonElement?>(
            null to null,
            "string" to JsonPrimitive("string"),
            'x' to JsonPrimitive("x"),
            true to JsonPrimitive(true),
            123 to JsonPrimitive("123"),
            SampleEnum.KEY to JsonPrimitive("KEY"),
            listOf(1, 2) to JsonArray(listOf(JsonPrimitive("1"), JsonPrimitive("2"))),
            mapOf("k" to "v") to JsonObject(mapOf("k" to JsonPrimitive("v"))),
            intArrayOf(1, 2) to JsonArray(listOf(JsonPrimitive(1), JsonPrimitive(2))),
            doubleArrayOf(1.5, 2.5) to JsonArray(
                listOf(
                    JsonPrimitive(1.5),
                    JsonPrimitive(2.5),
                ),
            ),
            booleanArrayOf(true, false) to JsonArray(
                listOf(
                    JsonPrimitive(true),
                    JsonPrimitive(false),
                ),
            ),
            charArrayOf('a', 'b') to JsonArray(listOf(JsonPrimitive("a"), JsonPrimitive("b"))),
        )
        for ((input, expected) in cases) {
            input.toJsonElement() shouldBe expected
        }
    }

    @Test
    fun toJsonObject_stringifiesKeysAndDropsNulls() {
        val map = mapOf(1 to "a", null to "b", "x" to null)
        val obj = map.toJsonObject()
        obj shouldBe JsonObject(mapOf("1" to JsonPrimitive("a")))
    }

    @Test
    fun toJsonArray_ignoresNullElements() {
        val list = listOf("a", null, "b")
        val arr = list.toJsonArray()
        arr shouldBe JsonArray(listOf(JsonPrimitive("a"), JsonPrimitive("b")))
    }
    
// end-testfunctions
}

