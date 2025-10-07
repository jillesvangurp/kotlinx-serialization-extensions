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
[extension functions](https://github.com/jillesvangurp/kotlinx-serialization-extensions/blob/main/src/commonMain/kotlin/com/jillesvangurp/serializationext/extensions.kt) here.

The code snippet below documents usage of these via the test cases in                 
[JsonExtensionFunctionsTest](https://github.com/jillesvangurp/kotlinx-serialization-extensions/blob/main/src/commonTest/kotlin/com/jillesvangurp/serializationext/JsonExtensionFunctionsTest.kt).

```kotlin
// 👇 Build a simple JSON object with nested structures and various types
val user = buildJsonObject {
  put("id", 123)
  put("name", "Alice")
  put("isActive", true)
  put("tags", buildJsonArray { add("kotlin"); add("serialization") })
  put(
    "address",
    buildJsonObject {
      put("city", "Berlin")
      put("zip", "10115")
    },
  )
}

// 👇 Access primitives safely
user.getString("name") shouldBe "Alice"
user.getLong("id") shouldBe 123
user.getBoolean("isActive") shouldBe true
user.getString("missing").shouldBeNull()

// 👇 Nested lookup using vararg path
user.getString("address", "city") shouldBe "Berlin"
user.getString("address", "zip") shouldBe "10115"

// 👇 Arrays as Kotlin lists
user.getStringArray("tags") shouldBe listOf("kotlin", "serialization")

// 👇 Modify JSON non-destructively
val updatedUser = user.modify {
  put("country", "Germany")
}
updatedUser.getString("country") shouldBe "Germany"
user.getString("country").shouldBeNull() // original unchanged

// 👇 Remove keys cleanly
val trimmed = updatedUser.deleteKeys("isActive")
trimmed.getBooleanOrNull("isActive").shouldBeNull()

// 👇 Convert Kotlin structures to JSON
val skills = listOf("KMP", "Coroutines", "Serialization").toJsonElement()
val metadata = mapOf("role" to "Developer", "level" to 3).toJsonElement()

skills shouldBe JsonArray(listOf(JsonPrimitive("KMP"), JsonPrimitive("Coroutines"), JsonPrimitive("Serialization")))
metadata shouldBe JsonObject(mapOf("role" to JsonPrimitive("Developer"), "level" to JsonPrimitive("3")))

// 👇 Combine everything
val profile = buildJsonObject {
  put("user", user)
  put("skills", skills)
  put("metadata", metadata)
}

// 👇 Deep clone is safe to modify independently
val clone = profile.clone()
(clone as JsonObject).getObject("user")?.getString("name") shouldBe "Alice"

// 👇 Final consistency check
clone.getObject("metadata")!!.getString("role") shouldBe "Developer"
```

## License

All code is licensed under the [MIT License](LICENSE).

## Multi platform

This is a Kotlin Multiplatform library that should work on most kotlin platforms (jvm, js, ios, android, wasm, etc). 

