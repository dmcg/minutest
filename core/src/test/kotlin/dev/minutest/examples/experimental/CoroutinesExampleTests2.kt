package dev.minutest.examples.experimental

import dev.minutest.ContextBuilder
import dev.minutest.TestDescriptor
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
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

        fixture { Fixture() }

        // Now you can define coTest to use the Fixture
        coTest("use runBlocking") {
            assertEquals("bananarama", subject.add("banana", "rama"))
            advanceUntilIdle()
            assertEquals(10_000, currentTime)
        }

        after {
            // TestCoroutineScope holds some resources
            cleanupTestCoroutines()
        }
    }
}

@ExperimentalCoroutinesApi
private fun <F: TestCoroutineScope> ContextBuilder<F>.coTest(
    name: String,
    f: suspend F.(testDescriptor: TestDescriptor) -> Unit
) =
    test(name) { testDescriptor ->
        this@test.runBlockingTest {
            f.invoke(this@test, testDescriptor)
        }
    }

