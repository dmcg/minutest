package dev.minutest.examples.experimental

import dev.minutest.ContextBuilder
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test2
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals

@ExperimentalCoroutinesApi
class CoroutinesExampleTests : JUnit5Minutests {

    fun tests() = rootContext<String> {

        fixture { "banana" }

        // You can use runBlockingTest in your test
        test2("use runBlocking") {
            runBlockingTest {
                assertEquals("bananarama", it.slowPlus("rama"))
                advanceUntilIdle()
                assertEquals(10_000, currentTime)
            }
        }

        // Or define an extension function on ContextBuilder
        coTest("use coTest") { fixture, testCoroutineScope ->
            assertEquals("bananarama", fixture.slowPlus("rama"))
            testCoroutineScope.advanceUntilIdle()
            assertEquals(10_000, testCoroutineScope.currentTime)
        }
    }

}

suspend fun String.slowPlus(other: String): String = (this + other).also {
    delay(10_000)
}

@ExperimentalCoroutinesApi
private fun <F> ContextBuilder<F>.coTest(
    name: String,
    f: suspend F.(fixture: F, testCoroutineScope: TestCoroutineScope) -> Unit
) =
    test2(name) { fixture ->
        runBlockingTest {
            (fixture).f(fixture, this)
        }
    }

