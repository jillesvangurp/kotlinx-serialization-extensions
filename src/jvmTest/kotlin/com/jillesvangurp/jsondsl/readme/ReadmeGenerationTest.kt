package com.jillesvangurp.jsondsl.readme

import com.jillesvangurp.jsondsl.*
import com.jillesvangurp.kotlin4example.SourceRepository
import java.io.File
import kotlin.test.Test
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.encodeToJsonElement
import serializationext.DEFAULT_JSON
import serializationext.DEFAULT_PRETTY_JSON

const val githubLink = "https://github.com/formation-res/pg-docstore"

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
            +"""
            The main feature of [kotlin4example](https://github.com/jillesvangurp/kotlin4example) is of course integrating code samples into your documentation.   
        """
                .trimIndent()
            subSection("Json") {
                @Serializable
                data class Foo(val bar: String)
                example {
                    println( DEFAULT_JSON.encodeToString(Foo("foo")))
                    println( DEFAULT_PRETTY_JSON.encodeToString(Foo("foo")))
                }.let {
                    mdCodeBlock(it.stdOut, type = "text")
                }
            }
        }
        includeMdFile("outro.md")
    }
