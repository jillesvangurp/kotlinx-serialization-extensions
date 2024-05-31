package com.jillesvangurp.jsondsl.readme

import com.jillesvangurp.kotlin4example.SourceRepository
import com.jillesvangurp.serializationext.DEFAULT_JSON
import com.jillesvangurp.serializationext.DEFAULT_PRETTY_JSON
import java.io.File
import kotlin.test.Test
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

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

        section("Example usage") {

            subSection("Using DEFAULT_JSON and DEFAULT_PRETTY_JSON") {

                +"""
                    Because the defaults for Json in kotlinx.serialization are a bit dangerous, this
                    library has alternatives with more sane settings.
                    
                    Particularly, it's designed to be lenient, not serialize nulls, 
                    not fail on missing keys, enum values, etc.
                    
                    This is important for forward and backward compatibility. Also many languages 
                    don't have default values for parameters so omitting default values is just
                    weird and actively harmful. And a closed world assumption of everybody using
                    kotlin and kotlinx.serialization simply breaks in the real world.
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
            subSection("Extension functions") {
                exampleFromSnippet("com/jillesvangurp/serializationext/JsonExtensionFunctionsTest.kt","testfunctions", allowLongLines = true)
            }
        }
        includeMdFile("outro.md")
    }
