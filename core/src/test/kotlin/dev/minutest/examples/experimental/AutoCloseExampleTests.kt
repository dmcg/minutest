package dev.minutest.examples.experimental

import dev.minutest.experimental.autoClose
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test
import java.io.BufferedWriter
import java.nio.file.Files
import kotlin.test.assertEquals

class AutoCloseExampleTests : JUnit5Minutests {

    val tempFile = Files.createTempFile("temp", ".txt").toFile()

    fun tests() = rootContext {

        // autoclose values are lazily created and disposed after all
        // tests in the context are complete
        val sharedResource: BufferedWriter by autoClose {
            tempFile.bufferedWriter()
        }

        test("test 1") {
            // sharedResource is created on its first use
            synchronized(sharedResource) {
                // synchronised because test may be in parallel
                sharedResource.appendLine("in test 1")
            }
        }

        test("test 2") {
            // this is the same instance
            synchronized(sharedResource) {
                sharedResource.appendLine("in test 2")
            }
        }

        afterAll {
            // SharedResource has already been closed by now.
            // Compare sets to cope with execution order.
            assertEquals(
                setOf("in test 1", "in test 2"),
                tempFile.readLines().toSet()
            )
        }
    }
}