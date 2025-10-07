# Kotlinx Serialization Extensions

[![Process Pull Request](https://github.com/jillesvangurp/kotlinx-serialization-extensions/actions/workflows/pr_master.yaml/badge.svg)](https://github.com/jillesvangurp/kotlinx-serialization-extensions/actions/workflows/pr_master.yaml)

This is a small library that fixes the default settings for `Json()` in `kotlinx.serialization` that should
benefit anyone that uses that for e.g. use in servers or API clients.

I got tired of copy pasting these between essentially every project where I use `kotlinx.serialization`, so I created this library. This allows me to pull this in everywhere I need this and gives me a central place to manage these defaults.

Features

- DEFAULT_JSON and DEFAULT_PRETTY_JSON Json configurations with some sane defaults. The actual defaults in kotlinx.serialization are wrong for anyone looking to implement forward compatible APIs. 
- Some extension functions on various things that make working with `JsonElement` in kotlinx a bit less painful. 

See the documentation below for more details.

## Gradle

This library is published to our own maven repository.

```kotlin
repositories {
    mavenCentral()
    maven("https://maven.tryformation.com/releases") {
        // optional but it speeds up the gradle dependency resolution
        content {
            includeGroup("com.jillesvangurp")
            includeGroup("com.tryformation")
        }
    }
}
```

And then you can add the dependency:

```kotlin
    // check the latest release tag for the latest version
    implementation("com.jillesvangurp:kotlinx-serialization-extensions:1.x.y")
```

## Defaults used

The code snippet below is the two `Json` instances with sane defaults. 
See the comments in the code for what has been configured and why.                                                              

```kotlin
/**
 * Sane/safe defaults for [Json].
 */
val DEFAULT_JSON: Json = Json {
  // preserving defaults is important
  // don't assume users of your json has access to your model classes
  encodeDefaults = true
  // no new lines, useful if you are generating e.g. ndjson or
  // just want to save space on redundant whitespace
  prettyPrint = false
  // tolerate minor json issues in favor of still being able to parse things
  // this allows you to handle the quite common case of e.g. numbers being encoded as strings ("42")
  // and still parse this correctly
  isLenient = true
  // encoding nulls can waste a lot of space and client code should not depend on nulls being
  // present to begin with . Unless you have parsing logic
  // depending on explicit nulls (why?!) don't turn this on
  explicitNulls = false
  // forward compatibility: new fields in the json are OK, just ignore them
  // true is critical for clients when servers add new fields.
  // Without it, even harmless additions can break consumers.
  ignoreUnknownKeys = true
  // forward compatibility: ignore unknown enum values
  coerceInputValues = true
  // handle serialized NaN and infinity double values instead of having them default to null
  allowSpecialFloatingPointValues = true
}

/**
 * Same as [DEFAULT_JSON] with pretty printing turned on
 */
val DEFAULT_PRETTY_JSON: Json = Json {
  encodeDefaults = true
  prettyPrint = true
  isLenient = true
  explicitNulls = false
  ignoreUnknownKeys = true
  coerceInputValues = true
  allowSpecialFloatingPointValues = true
}
```

By default, Json() is too strict—throwing errors on missing keys, unknown keys, 
unexpected enum strings, or null where not expected. This behavior is not appropriate
for normal servers because it can break working client code with seemingly simple 
changes. And it's also not appropriate for client parsing code because any server 
changes might break previously working client code.

In other words, the defaults only are appropriate when you can tightly control 
both sides, but fails in more common systems where spec drift is normal, 
not all code uses Kotlin, or where client and server code evolve at different pace.                                    
The kotlinx.serialization defaults are probably wrong for common use in API clients, code for rendering server responses, or any similar use cases for the following reasons:
                
- They expose you to forward/backward compatibility issues
- They make assumptions about the client parser capabilities/features that parses your json  
- Encoding nulls means you generate a lot of bloated Json for sparsely 
populated objects with a lot of null values.                

## Example usage

### Using DEFAULT_JSON and DEFAULT_PRETTY_JSON

Because the defaults for Json in kotlinx.serialization are a bit problematic, this
library provides alternatives with more appropriate settings.

Particularly, it's designed to be lenient, not serialize nulls, 
not fail on missing keys, enum values, etc.

This is important for forward and backward compatibility. Also many languages 
don't have default values for parameters so omitting default values is just
weird and problematic for such clients; especially if they are non trivial. 

A closed world assumption of everybody using kotlin and kotlinx.serialization 
simply is not appropriate for either client code or server code. Making your 
client reject minor changes in responses makes your client code brittle. Having 
your server reject previously valid JSON after minor changes breaks 
compatibility 

```kotlin
@Serializable
data class Foo(val bar: String, val baz: String?=null)

val value = Foo("foo")

// use just like you would use your Json() instance
println(DEFAULT_JSON.encodeToString(value))
println(DEFAULT_PRETTY_JSON.encodeToString(value))
```

```text
{"bar":"foo"}
{
  "bar": "foo"
}
```

## Misc. open issues in kotlinx.serialization and caveats

There are of course some valid reasons why the kotlinx.serialization defaults are the way they are. This includes
a few open bugs:

- [coerceInputValues prevent custom enum serializer to be used](https://github.com/Kotlin/kotlinx.serialization/issues/1947)
- [Unexpected MissingFieldException when decoding explicit null value from json for nullable enum](https://github.com/Kotlin/kotlinx.serialization/issues/2170)

Other considerations:

- `encodeDefaults = true` and `explicitNulls = false` may cause some confusion 
with **non null default values being omitted for null values**. That's probably the reason the defaults are inverted in `kotlinx.serialization`.
But as noted, that causes other issues with needless bloat in the form of null json values and relying
on client code to be able to generate the same defaults. It's better to write code that doesn't rely
on this kind of magic.
- `coerceInputValues = true` can silently coerce out-of-range numbers to defaults (e.g., Long overflow). 
Great for resilience, but it can mask data quality issues.
- allowSpecialFloatingPointValues = true improves robustness but may violate 
strict JSON consumers downstream. IMHO this is better than dropping values or 
defaulting them to 0.0 but there can be cases where this is not ideal. 

If you need different defaults, you can of course simply copy and adapt the above as needed.

## Extension Functions Overview

In addition to sane defaults, I've also bundled a few extension functions in this 
library that make working with json elements a bit nicer. 

These extensions provide convenience functions for working with `JsonObject`, 
`JsonArray`, and related types in **kotlinx.serialization**. They add safe getters 
(`getObject`, `getString`, `getDouble`, etc.), array extractors (`getStringArray`, 
`getDoubleArray`), and mutators (`set`, `deleteKeys`). 

They also include generic conversion utilities (`toJsonElement`, `toJsonArray`, 
`toJsonObject`) that turn common Kotlin types—scalars, enums, maps, collections, 
sequences, and primitive arrays—into `JsonElement`s, making it easy to build or 
manipulate JSON structures idiomatically in Kotlin.                               

You can find the 
[extension functions](https://github.com/formation-res/kotlinx-serialization-extensions/blob/main/src/commonMain/kotlin/com/jillesvangurp/serializationext/extensions.kt) here.

The code snippet below documents usage of these via the test cases in                 
[JsonExtensionFunctionsTest](https://github.com/formation-res/kotlinx-serialization-extensions/blob/main/src/commonTest/kotlin/com/jillesvangurp/serializationext/JsonExtensionFunctionsTest.kt).

```kotlin
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

  fun modify_jsonObject() {
    val obj = buildJsonObject {}
    obj.modify {}

  }
}
```

## License

All code is licensed under the [MIT License](LICENSE).

## Multi platform

This is a Kotlin Multiplatform library that should work on most kotlin platforms (jvm, js, ios, android, wasm, etc). 

