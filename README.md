# Kotlinx Serialization Extensions

[![Process Pull Request](https://github.com/jillesvangurp/kotlinx-serialization-extensions/actions/workflows/pr_master.yaml/badge.svg)](https://github.com/jillesvangurp/kotlinx-serialization-extensions/actions/workflows/pr_master.yaml)

This is a small library that fixes the default settings for `Json()` in `kotlinx.serialization` that should
benefit anyone that uses that for e.g. use in servers or API clients and wants to prevent 'unnecessary' parsing errors rather than being very strict about what is acceptable as input. I'm aware this is not for everyone; but I'm in the camp of not wanting to deal with trivial parsing failures on what otherwise are perfectly acceptable and reasonable requests. I prefer using validation for the content rather than the syntax.

I got tired of copy pasting these between essentially every project where I use `kotlinx.serialization`, so I created this library. This allows me to pull this in everywhere I need this and gives me a central place to manage these defaults.

Features
- 
- General principle of being strict about what we send being 100% valid JSON and flexible on what we parse and not fail over minor syntax issues.
- DEFAULT_JSON and DEFAULT_PRETTY_JSON Json configurations with some sane defaults. The actual defaults in kotlinx.serialization are wrong for anyone looking to implement forward/backward compatible APIs or supporting an ecosystem where not everything is implemented in Kotlin (e.g. default value handling).
- Misc. extension functions on various things that make working with `JsonElement`, `JsonObject`, `JsonArray`, and `JsonPrimitive` in kotlinx a bit easier and kotlin friendly. 

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
 * Sane/safe defaults for [Json] that maximize forward/backward compatibility.
 *
 * The goal of these defaults is to make a best effort to parse json and not fail parsing needlessly
 * over recoverable situations like omitting nullable fields, having enum values that are new, extra
 * fields that aren't in your model, etc. If you want to be strict about this, then don't
 * use this library.
 *
 * Also has some sane defaults for generating Json that ensure the other side doesn't have to be
 * a Kotlin program using the exact same model classes and kotlinx.serialization configuration.
 */
val DEFAULT_JSON: Json = Json {
  // defaults that maximize compatibility for parsing:

  // tolerate minor json issues in favor of still being able to parse things
  // this allows you to handle the quite common case of e.g. numbers being encoded as strings ("42")
  // and still parse this correctly.
  isLenient = true
  // forward compatibility: new fields in the json are OK, just ignore them
  // true is critical for clients when servers add new fields.
  // Without it, even harmless API optional field additions can break consumers.
  ignoreUnknownKeys = true
  // forward compatibility: ignore unknown enum values
  coerceInputValues = true
  // handle serialized NaN and infinity double values instead of having them default to null
  allowSpecialFloatingPointValues = true
  // allow people to use comments with // and /* ... */
  allowComments = true
  // allow trailing commas for list items / map entries as is common in Javascript; and indeed Kotlin
  allowTrailingComma = true

  // defaults that maximize compatibility for serialized json:

  // no new lines, useful if you are generating e.g. ndjson or
  // just want to save space on redundant whitespace
  prettyPrint = false
  // preserving defaults is important
  // don't assume users of your json has access to your model classes with their defaults and
  // explicitly encode defaults
  encodeDefaults = true
  // encoding nulls can waste a lot of space and client code should not depend on nulls being
  // present to begin with . Unless you have parsing logic
  // depending on explicit nulls (why?!) don't turn this on
  explicitNulls = false

}

/**
 * Same as [DEFAULT_JSON] with pretty printing turned on
 */
val DEFAULT_PRETTY_JSON: Json = Json {
  isLenient = true
  ignoreUnknownKeys = true
  coerceInputValues = true
  allowSpecialFloatingPointValues = true
  allowComments = true
  allowTrailingComma = true

  encodeDefaults = true
  prettyPrint = true
  explicitNulls = false
}
```

By default, Json() is very strict — throwing errors on missing keys, unknown keys, 
unexpected enum strings, or null where not expected.

## Strictness vs. best effort parsing

This behavior can be too strict
for normal servers because it can break previously working client code with 
seemingly simple and conservative API
changes making requests that are otherwise still fine and processable. 

Likewise, it's also not ideal for client parsing code to break over                                 
such server API changes. E.g. new enum values or fields could be ignored instead 
of breaking your code. This library configures defaults that result in a 
best effort attempt to parse json. It won't fail over trivial stuff like comments, 
trailing comments, etc.

The defaults in kotlinx.serialization only are appropriate when you can tightly control 
both sides (parsing and serialization) and ideally enforce both sides using Kotlin, the same model classes, 
and the same strict parser configuration. Such a closed world assumption is 
conservative but not appropriate for everyone.               
                                    
The kotlinx.serialization defaults are probably not ideal for common use in API clients, 
code for rendering server responses, or any similar use cases for the following reasons:
                
- They expose you to forward/backward compatibility issues. Any minor server API changes 
could break clients. You don't handle an optional/nullable field? Error. 
A new enum value appears that you can't handle? Error. You try to parse a nullable
 field that is no longer there (with a null value) -> Error.
- They make assumptions about the client parser capabilities/features that parses your json. 
Like having default values for class properties; or the same defaults.
- Encoding nulls means you generate a lot of bloated Json for sparsely 
populated objects with a lot of null values. The resulting JSON is still 100% valid 
JSON if you omit nulls. If your parsing code depends on nulls being explicitly there, 
you are just being overly strict with your parser and the resulting code is 
probably needlessly brittle.          
    
Being too strict can lead to preventable parser failures in more common/open eco systems where spec 
drift/evolution over time is expected and normal, not everyone uses Kotlin, or where client 
and server code evolve at a different pace.
                    
There are of course valid reasons to want to be more strict for compliance or 
security reasons. If so, configure your own defaults. But otherwise these defaults should 
be safe and sane for any API clients or servers written in Kotlin.

In various discussions with people I've noticed that this is actually a very divisive topic. 
IMHO the defaults in kotlinx.serialization are actively harmful and wrong. That's 
just my opinion and I've met people that strongly disagree with that and get very 
upset over this.  

If you fall on that side of the debate, this library is not for you and more power to you. 
But if you are on the fence, you might want to consider that there's a good reason that 
the options this library configures exist: there just is an enormous amount 
of stuff that would break without them. The JSON world is messy and you don't always
get to be strict about what people send you. Sometimes json just has trailing 
comments, missing keys for nullable properties, or comments. And some libraries 
happily serialize NaN into the JSON and you just need to deal with that when that happens.

If you have found yourself repeatedly tweaking your Json configuration over such issues, 
this library is for you.          

## Example usage

### Using DEFAULT_JSON and DEFAULT_PRETTY_JSON to generate 100% valid JSON

The main difference between `DEFAULT_JSON` and `DEFAULT_PRETTY_JSON` is 
that one pretty prints and the other one doesn't.

```kotlin
@Serializable
data class Foo(val bar: String, val baz: String?=null,val barr:String?="barrr")

val value = Foo("foo")

// use just like you would use your Json() instance
println(DEFAULT_JSON.encodeToString(value))
println(DEFAULT_PRETTY_JSON.encodeToString(value))
```

```text
{"bar":"foo","barr":"barrr"}
{
  "bar": "foo",
  "barr": "barrr"
}
```

Note how it does not encode the baz property because it is null. That's intentional.
If your parser breaks over missing null values, it's because it's being overly strict.

Most Json serializers out there for other languages usually omit nulls. There 
are few good reason to emit null values. Unless you have to support overly strict parsing 
logic (e.g. some patch or merge logic might depend on this) that you need to keep happy. 
Otherwise, you can save yourself the bandwidth. Which for some sparsely 
populated JSON can add up to be substantial. I've never encountered any use 
cases that require nulls (outside of brittle APIs 
using kotlinx.serialization defaults).

Also note how the barr value is there even though the property is nullable. 
Not all other languages have default values. For example Java doesn't have those. 
Emitting default values is both safe and shouldn't break any parser logic. 
Building in assumptions about default value handling into your serialization 
logic is a bad idea.

## Misc. open issues in kotlinx.serialization and caveats

There are of course some valid reasons why the kotlinx.serialization defaults are the way they are. This includes
a few open bugs:

- [coerceInputValues prevent custom enum serializer to be used](https://github.com/Kotlin/kotlinx.serialization/issues/1947)
- [Unexpected MissingFieldException when decoding explicit null value from json for nullable enum](https://github.com/Kotlin/kotlinx.serialization/issues/2170) (Closed)
- [coerceInputValues=true and explicitNulls=false should decode unknown enum value without default to null](https://github.com/Kotlin/kotlinx.serialization/issues/2586) (Closed)

The closed issues are of course non issues if you keep your libraries up to date.

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

In addition to the defaults above, I've also bundled a few extension functions in this 
library that make working with json elements a bit nicer. 

These extensions provide convenience functions for working with `JsonObject`, 
`JsonArray`, and related types in **kotlinx.serialization**. They add safe getters 
(`getObject`, `getString`, `getDouble`, etc.), array extractors (`getStringArray`, 
`getDoubleArray`), and mutators (`set`, `deleteKeys`). 

They also include generic conversion utilities (`toJsonElement`, `toJsonArray`, 
`toJsonObject`) that turn common Kotlin types—scalars, enums, maps, collections, 
sequences, and primitive arrays—into `JsonElement`s, making it easier to build or 
manipulate JSON structures idiomatically in Kotlin without having to create 
model classes for everything.                               

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

This is a Kotlin Multiplatform library that should work on all/most kotlin platforms where kotlinx.serialization works (jvm, js, ios, android, wasm, etc). Please, file a bug/pr if a platform you need is missing.

