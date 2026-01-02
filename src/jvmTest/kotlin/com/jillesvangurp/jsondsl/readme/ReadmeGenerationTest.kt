package com.jillesvangurp.jsondsl.readme

import com.jillesvangurp.kotlin4example.SourceRepository
import com.jillesvangurp.serializationext.DEFAULT_JSON
import com.jillesvangurp.serializationext.DEFAULT_PRETTY_JSON
import java.io.File
import kotlin.test.Test
import kotlinx.serialization.Serializable

const val githubLink = "https://github.com/jillesvangurp/kotlinx-serialization-extensions"

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
                    # Kotlinx Serialization Extensions
        
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
            """.trimIndent()

        }

        section("Example usage") {

            subSection("Using DEFAULT_JSON and DEFAULT_PRETTY_JSON to generate 100% valid JSON") {

                +"""
                    The main difference between `DEFAULT_JSON` and `DEFAULT_PRETTY_JSON` is 
                    that one pretty prints and the other one doesn't.
                """.trimIndent()
                example {
                    @Serializable
                    data class Foo(val bar: String, val baz: String?=null,val barr:String?="barrr")

                    val value = Foo("foo")

                    // use just like you would use your Json() instance
                    println(DEFAULT_JSON.encodeToString(value))
                    println(DEFAULT_PRETTY_JSON.encodeToString(value))
                }.let {
                    mdCodeBlock(it.stdOut, type = "text")
                }

                +"""
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
                """.trimIndent()
            }

        }

        section("Misc. open issues in kotlinx.serialization and caveats") {
            +"""
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
            """.trimIndent()

        }

        section("Extension Functions Overview") {
            +"""
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
                ${
                mdLinkToRepoResource(
                    "extension functions",
                    "/blob/main/src/commonMain/kotlin/com/jillesvangurp/serializationext/extensions.kt",
                )
            } here.

                The code snippet below documents usage of these via the test cases in                 
                ${
                mdLinkToRepoResource(
                    "JsonExtensionFunctionsTest",
                    "/blob/main/src/commonTest/kotlin/com/jillesvangurp/serializationext/JsonExtensionFunctionsTest.kt",
                )
            }.
            """.trimIndent()
            exampleFromSnippet(
                "com/jillesvangurp/serializationext/JsonExtensionFunctionsTest.kt",
                "testfunctions",
                allowLongLines = true,
            )
        }
        includeMdFile("outro.md")
    }
