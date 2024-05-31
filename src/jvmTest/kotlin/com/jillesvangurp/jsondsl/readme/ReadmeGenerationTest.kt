package com.jillesvangurp.jsondsl.readme

import com.jillesvangurp.kotlin4example.SourceRepository
import com.jillesvangurp.serializationext.DEFAULT_JSON
import com.jillesvangurp.serializationext.DEFAULT_PRETTY_JSON
import java.io.File
import kotlin.test.Test
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

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
            @Serializable
            data class Foo(val bar: String)
            example {
                println(DEFAULT_JSON.encodeToString(Foo("foo")))
                println(DEFAULT_PRETTY_JSON.encodeToString(Foo("foo")))
            }.let {
                mdCodeBlock(it.stdOut, type = "text")
            }
        }
        includeMdFile("outro.md")
    }
