package com.jillesvangurp.serializationext

import io.kotest.matchers.nulls.shouldBeNull
import kotlinx.serialization.json.*
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class JsonElementExtensionsTest {

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
            jsonObject.getArray("array") shouldBe JsonArray(listOf(JsonPrimitive("one"), JsonPrimitive("two")))
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

        // Non-destructive update: returns a copy with the provided key/value set
        @Test
        fun set_addsOrReplacesEntry() {
            val jsonObject = buildJsonObject {
                put("existing", "value")
            }
            val updatedJsonObject = jsonObject.set("newKey" to JsonPrimitive("newValue"))
            updatedJsonObject shouldBe JsonObject(
                mapOf(
                    "existing" to JsonPrimitive("value"),
                    "newKey" to JsonPrimitive("newValue")
                )
            )
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
                    JsonArray(listOf(JsonPrimitive("1"), JsonPrimitive("2"), JsonPrimitive("3")))
                )
            )
        }

        // Generic Map -> JsonElement (JsonObject) conversion: supports primitives, maps, and lists
        @Test
        fun map_toJsonElement_serializesSupportedTypes() {
            val map = mapOf(
                "string" to "value",
                "number" to 42,
                "nestedMap" to mapOf("key" to "value"),
                "list" to listOf(1, 2, 3)
            )
            val jsonObject = map.toJsonElement()

            jsonObject shouldBe JsonObject(
                mapOf(
                    "string" to JsonPrimitive("value"),
                    "number" to JsonPrimitive("42"),
                    "nestedMap" to JsonObject(mapOf("key" to JsonPrimitive("value"))),
                    "list" to JsonArray(listOf(JsonPrimitive("1"), JsonPrimitive("2"), JsonPrimitive("3")))
                )
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
    }
// end-testfunctions
}
