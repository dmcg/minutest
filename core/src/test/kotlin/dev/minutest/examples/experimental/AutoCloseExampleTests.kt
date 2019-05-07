package dev.minutest.examples.experimental

import dev.minutest.experimental.autoClose
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import java.io.BufferedWriter
import java.nio.file.Files
import kotlin.test.assertEquals

class AutoCloseExampleTests : JUnit5Minutests {

    val tempFile = Files.createTempFile("temp", ".txt").toFile()

    fun tests() = rootContext {

        // autoclose values are lazily created and disposed after all tests in the context are complete
        val sharedResource: BufferedWriter by autoClose {
            tempFile.bufferedWriter()
        }

        test("test 1") {
            // sharedResource is not created until here
            sharedResource.appendln("in test 1")
        }

        test("test 2") {
            // this is the same instance
            sharedResource.appendln("in test 2")
        }

        afterAll {
            // sharedResource has already been closed by now
            assertEquals(listOf("in test 1", "in test 2"), tempFile.readLines())
        }
    }
}