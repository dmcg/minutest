package dev.minutest.examples.experimental

import dev.minutest.*
import dev.minutest.junit.JUnit5Minutests
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals

// If your subject requires a CoroutineScope
class Subject(private val scope: CoroutineScope) {
    suspend fun add(a: String, b: String): String {
        var result: String? = null
        scope.launch {
            result = a.slowPlus(b)
        }.join()
        return result!!
    }
}

@ExperimentalCoroutinesApi
class CoroutinesExampleTests2 : JUnit5Minutests {

    // Build your TestCoroutineScope into the fixture so that you can give it to the subject
    class Fixture(
        private val testCoroutineScope: TestCoroutineScope = TestCoroutineScope()
    ): TestCoroutineScope by testCoroutineScope {
        val subject = Subject(testCoroutineScope)
    }

    fun tests() = rootContext<Fixture> {

        given { Fixture() }

        // Now you can define coTest to use the Fixture
        coTest("fixture is now a TestCoroutineScope") {
            assertEquals("bananarama", subject.add("banana", "rama"))
            advanceUntilIdle()
            assertEquals(10_000, currentTime)
        }

        afterEach {
            // TestCoroutineScope holds some resources
            cleanupTestCoroutines()
        }
    }
}

@ExperimentalCoroutinesApi
private fun <F: TestCoroutineScope> ContextBuilder<F>.coTest(
    name: String,
    f: suspend F.(fixture: F) -> Unit
) =
    test2(name) { fixture ->
        fixture.runBlockingTest {
            fixture.f(fixture)
        }
    }

