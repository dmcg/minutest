package dev.minutest.examples.experimental

import dev.minutest.ContextBuilder
import dev.minutest.TestDescriptor
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals

@ExperimentalCoroutinesApi
class CoroutinesExampleTests : JUnit5Minutests {

    fun tests() = rootContext<String> {

        fixture { "banana" }

        test("use runBlocking") {
            runBlockingTest {
                assertEquals("bananarama", fixture.slowPlus("rama"))
                advanceUntilIdle()
            }
        }

        coTest("use coTest") { _, testCoroutineScope ->
            assertEquals("bananaram", fixture.slowPlus("rama"))
            testCoroutineScope.advanceUntilIdle()
        }
    }
}

suspend fun String.slowPlus(other: String): String = (this + other).also {
    delay(10_000)
}

fun <F> ContextBuilder<F>.coTest(
    name: String,
    f: suspend F.(testDescriptor: TestDescriptor, testCoroutineScope: TestCoroutineScope) -> Unit) =
    test(name) { testDescriptor ->
        runBlockingTest {
            f.invoke(this@test, testDescriptor, this)
        }
    }

