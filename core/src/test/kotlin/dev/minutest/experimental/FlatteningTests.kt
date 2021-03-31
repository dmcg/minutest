package dev.minutest.experimental

import dev.minutest.*
import dev.minutest.testing.runTests
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.fail
import org.opentest4j.MultipleFailuresError


class FlatteningTests {

    private val testLogger = TestLogger()
    private val miscLog = mutableListOf<String>().synchronized()

    @Test fun `empty sequence`() {
        val tests = rootContext<Sequence<String>> {
            logTo(testLogger)

            given { emptySequence() }
            context_<String>("flattened") {

                flatten()

                test("should not be run") {
                    fail("")
                }

                afterEach {
                    miscLog.add("after")
                }
            }
        }
        // We see the contexts opened because, while the test is not run, the wrapper that
        // flattens is run.
        checkLog(tests,
            "▾ root",
            "▾ root/flattened"
        )
    }

    @Test fun `each item is tested`() {
        val tests = rootContext<Sequence<String>> {
            logTo(testLogger)

            given { sequenceOf("one", "two", "three") }
            context_<String>("flattened") {

                flatten()

                beforeEach {
                    miscLog.add("before $it")
                }

                test("is a string") {
                    miscLog.add("test $it")
                    @Suppress("USELESS_IS_CHECK")
                    assertTrue(it is String)
                }

                afterEach {
                    miscLog.add("after $it")
                }
            }
        }
        checkLog(tests,
            "▾ root",
            "▾ root/flattened",
            "✓ root/flattened/is a string",
            "✓ root/flattened/is a string",
            "✓ root/flattened/is a string",
        )
        assertLoggedInAnyOrder(miscLog,
            "before one",
            "test one",
            "after one",
            "before two",
            "test two",
            "after two",
            "before three",
            "test three",
            "after three"
        )
    }

    @Test fun `throws single MultipleFailuresError with failures`() {
        val tests = rootContext<Sequence<String>> {
            logTo(testLogger)
            given { sequenceOf("one", "two", "three") }
            context_<String>("flattened") {

                flatten()

                beforeEach {
                    miscLog.add("before $it")
                }

                test("is two") {
                    miscLog.add("test $it")
                    assertEquals("two", it)
                }

                afterEach {
                    miscLog.add("after $it")
                }
            }
        }
        val allErrors = checkLog(tests,
            "▾ root",
            "▾ root/flattened",
            "X root/flattened/is two",
            "✓ root/flattened/is two",
            "X root/flattened/is two",
        )
        assertAll(
            { assertEquals(1, allErrors.size) },
            { assertEquals(2, (allErrors[0] as MultipleFailuresError).failures.size) }
        )

        assertLoggedInAnyOrder(miscLog,
            "before one",
            "test one",
            "after one",
            "before two",
            "test two",
            "after two",
            "before three",
            "test three",
            "after three"
        )

    }

    private fun checkLog(tests: RootContextBuilder, vararg expected: String) =
        runTests(tests).also {
            assertLoggedInAnyOrder(testLogger.toStrings(), *expected)
        }
}