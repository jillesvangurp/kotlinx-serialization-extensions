package com.jillesvangurp.jsondsl.readme

import com.jillesvangurp.kotlin4example.SourceRepository
import com.jillesvangurp.serializationext.DEFAULT_JSON
import com.jillesvangurp.serializationext.DEFAULT_PRETTY_JSON
import java.io.File
import kotlin.test.Test
import kotlinx.serialization.Serializable

const val githubLink = "https://github.com/formation-res/kotlinx-serialization-extensions"

val sourceGitRepository =
    SourceRepository(
        repoUrl = githubLink,
        sourcePaths = setOf("src/commonMain/kotlin", "src/commonTest/kotlin", "src/jvmTest/kotlin"),
    )

class ReadmeGenerationTest {

    @Test
    fun `generate docs`() {
        File(".", "README.md")
            .writeText(
                """
            # JsonDsl

        """.trimIndent().trimMargin() +
                    "\n\n" +
                    readmeMd.value,
            )
    }
}

val readmeMd =
    sourceGitRepository.md {
        includeMdFile("intro.md")

        section("Defaults used") {
            +"""
                The code snippet below is the two `Json` instances with sane defaults. 
                See the comments in the code for what has been configured and why.                                                              
            """.trimIndent()

            exampleFromSnippet(
                "com/jillesvangurp/serializationext/defaults.kt",
                "jsondefaults",
                allowLongLines = true,
            )
            +"""
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
            """.trimIndent()

        }

        section("Example usage") {

            subSection("Using DEFAULT_JSON and DEFAULT_PRETTY_JSON") {

                +"""
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
                """.trimIndent()
                @Serializable
                data class Foo(val bar: String)
                example {
                    println(DEFAULT_JSON.encodeToString(Foo("foo")))
                    println(DEFAULT_PRETTY_JSON.encodeToString(Foo("foo")))
                }.let {
                    mdCodeBlock(it.stdOut, type = "text")
                }
            }

        }

        section("Misc. open issues in kotlinx.serialization and caveats") {
            +"""
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
            """.trimIndent()

        }

        section("Extension Functions Overview") {
            +"""
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
                ${mdLinkToRepoResource("extension functions","/blob/main/src/commonMain/kotlin/com/jillesvangurp/serializationext/extensions.kt")} here.

                The code snippet below documents usage of these via the test cases in                 
                ${mdLinkToRepoResource("JsonExtensionFunctionsTest",
                "/blob/main/src/commonTest/kotlin/com/jillesvangurp/serializationext/JsonExtensionFunctionsTest.kt")}.
            """.trimIndent()
            exampleFromSnippet(
                "com/jillesvangurp/serializationext/JsonExtensionFunctionsTest.kt",
                "testfunctions",
                allowLongLines = true,
            )
        }
        includeMdFile("outro.md")
    }
