package com.jillesvangurp.serializationext

import io.kotest.matchers.nulls.shouldBeNull
import kotlinx.serialization.json.*
import io.kotest.matchers.shouldBe
import kotlin.test.Test

enum class SampleEnum { KEY }

class JsonElementExtensionsTest {
// begin-testfunctions
    @Test
    fun testGetObject() {
        val jsonObject = buildJsonObject {
            put("nested", buildJsonObject { put("key", "value") })
        }
        jsonObject.getObject("nested") shouldBe JsonObject(mapOf("key" to JsonPrimitive("value")))
        jsonObject.getObject("nonexistent").shouldBeNull()
    }

    @Test
    fun testGetDouble() {
        val jsonObject = buildJsonObject {
            put("number", 42.0)
            put("nullValue", JsonNull)
        }
        jsonObject.getDouble("number") shouldBe 42.0
        jsonObject.getDouble("nullValue").shouldBeNull()
        jsonObject.getDouble("nonexistent").shouldBeNull()
    }

    @Test
    fun testGetArray() {
        val jsonObject = buildJsonObject {
            put("array", buildJsonArray { add("one"); add("two") })
            put("nullArray", JsonNull)
        }
        jsonObject.getArray("array") shouldBe JsonArray(listOf(JsonPrimitive("one"), JsonPrimitive("two")))
        jsonObject.getArray("nullArray").shouldBeNull()
        jsonObject.getArray("nonexistent").shouldBeNull()
    }

    @Test
    fun testGetStringArray() {
        val jsonObject = buildJsonObject {
            put("stringArray", buildJsonArray { add("one"); add("two") })
        }
        jsonObject.getStringArray("stringArray") shouldBe listOf("one", "two")
        jsonObject.getStringArray("nonexistent") shouldBe emptyList()
    }

    @Test
    fun testGetDoubleArray() {
        val jsonObject = buildJsonObject {
            put("doubleArray", buildJsonArray { add(1.0); add(2.0) })
        }
        jsonObject.getDoubleArray("doubleArray") shouldBe listOf(1.0, 2.0)
        jsonObject.getDoubleArray("nonexistent") shouldBe emptyList()
    }

    @Test
    fun testGetString() {
        val jsonObject = buildJsonObject {
            put("string", "value")
            put("nullValue", JsonNull)
        }
        jsonObject.getString("string") shouldBe "value"
        jsonObject.getString("nullValue").shouldBeNull()
        jsonObject.getString("nonexistent").shouldBeNull()
    }


    @Test
    fun testGetStringWithEnum() {
        val jsonObject = buildJsonObject {
            put("KEY", "value")
        }
        jsonObject.getString(SampleEnum.KEY) shouldBe "value"
        jsonObject.getString(SampleEnum.KEY.name) shouldBe "value"
    }

    @Test
    fun testSet() {
        val jsonObject = buildJsonObject {
            put("existing", "value")
        }
        val updatedJsonObject = jsonObject.set("newKey" to JsonPrimitive("newValue"))
        updatedJsonObject shouldBe JsonObject(mapOf("existing" to JsonPrimitive("value"), "newKey" to JsonPrimitive("newValue")))
    }

    @Test
    fun testDeleteKeys() {
        val jsonObject = buildJsonObject {
            put("key1", "value1")
            put("key2", "value2")
        }
        val updatedJsonObject = jsonObject.deleteKeys("key1")
        updatedJsonObject shouldBe JsonObject(mapOf("key2" to JsonPrimitive("value2")))
    }
    @Test
    fun testListToJsonElement() {
        val list = listOf("one", 2, mapOf("key" to "value"), listOf(1, 2, 3))
        val jsonArray = list.toJsonElement()

        jsonArray shouldBe JsonArray(listOf(
            JsonPrimitive("one"),
            JsonPrimitive("2"),
            JsonObject(mapOf("key" to JsonPrimitive("value"))),
            JsonArray(listOf(JsonPrimitive("1"), JsonPrimitive("2"), JsonPrimitive("3")))
        ))
    }

    @Test
    fun testMapToJsonElement() {
        val map = mapOf("string" to "value", "number" to 42, "nestedMap" to mapOf("key" to "value"), "list" to listOf(1, 2, 3))
        val jsonObject = map.toJsonElement()

        jsonObject shouldBe JsonObject(mapOf(
            "string" to JsonPrimitive("value"),
            "number" to JsonPrimitive("42"),
            "nestedMap" to JsonObject(mapOf("key" to JsonPrimitive("value"))),
            "list" to JsonArray(listOf(JsonPrimitive("1"), JsonPrimitive("2"), JsonPrimitive("3")))
        ))
    }

    @Test
    fun testEmptyListToJsonElement() {
        val list = emptyList<Any?>()
        val jsonArray = list.toJsonElement()

        jsonArray shouldBe JsonArray(emptyList())
    }

    @Test
    fun testEmptyMapToJsonElement() {
        val map = emptyMap<String, Any?>()
        val jsonObject = map.toJsonElement()

        jsonObject shouldBe JsonObject(emptyMap())
    }

    @Test
    fun testNullValuesInListToJsonElement() {
        val list = listOf(null, "test", null)
        val jsonArray = list.toJsonElement()

        jsonArray shouldBe JsonArray(listOf(JsonPrimitive("test")))
    }

    @Test
    fun testNullValuesInMapToJsonElement() {
        val map = mapOf("key1" to null, "key2" to "value")
        val jsonObject = map.toJsonElement()

        jsonObject shouldBe JsonObject(mapOf("key2" to JsonPrimitive("value")))
    }
// end-testfunctions
}
