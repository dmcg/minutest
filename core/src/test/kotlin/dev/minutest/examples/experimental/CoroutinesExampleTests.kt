package dev.minutest.examples.experimental

import dev.minutest.ContextBuilder
import dev.minutest.TestDescriptor
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals

class CoroutinesExampleTests : JUnit5Minutests {

    fun tests() = rootContext<String> {

        fixture { "banana" }

        test("use runBlocking") {
            runBlocking {
                assertEquals("bananarama", fixture.slowPlus("rama"))
            }
        }

        coTest("don't") {
            assertEquals("bananarama", fixture.slowPlus("rama"))
        }
    }
}

suspend fun String.slowPlus(other: String) = this + other

fun <F> ContextBuilder<F>.coTest(name: String, f: suspend F.(testDescriptor: TestDescriptor) -> Unit) =
    test(name) { testDescriptor -> runBlocking { f.invoke(this@test, testDescriptor) } }
