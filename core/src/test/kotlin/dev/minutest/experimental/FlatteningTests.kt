package dev.minutest.experimental

import dev.minutest.RootContextBuilder
import dev.minutest.assertLogged
import dev.minutest.executeTests
import dev.minutest.rootContext
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.fail
import org.opentest4j.MultipleFailuresError


class FlatteningTests {

    private val log = mutableListOf<String>()

    @Test fun `empty sequence`() {
        val tests = rootContext<Sequence<String>> {
            logTo(log)

            fixture { emptySequence() }
            derivedContext<String>("flattened") {

                flatten()

                test("should not be run") {
                    fail("")
                }

                after {
                    log.add("after")
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
            logTo(log)

            fixture { sequenceOf("one", "two", "three") }
            derivedContext<String>("flattened") {

                flatten()

                before {
                    log.add("before $fixture")
                }

                test("is a string") {
                    log.add("test $fixture")
                    @Suppress("USELESS_IS_CHECK")
                    assertTrue(fixture is String)
                }

                after {
                    log.add("after $fixture")
                }
            }
        }
        checkLog(tests,
            "▾ root",
            "▾ root/flattened",
            "before one",
            "test one",
            "✓ root/flattened/is a string",
            "after one",
            "before two",
            "test two",
            "✓ root/flattened/is a string",
            "after two",
            "before three",
            "test three",
            "✓ root/flattened/is a string",
            "after three")
    }

    @Test fun `throws single MultipleFailuresError with failures`() {
        val tests = rootContext<Sequence<String>> {
            logTo(log)
            fixture { sequenceOf("one", "two", "three") }
            derivedContext<String>("flattened") {

                flatten()

                before {
                    log.add("before $fixture")
                }

                test("is two") {
                    log.add("test $fixture")
                    assertEquals("two", fixture)
                }

                after {
                    log.add("after $fixture")
                }
            }
        }
        val allErrors = checkLog(tests,
            "▾ root",
            "▾ root/flattened",
            "before one",
            "test one",
            "X root/flattened/is two",
            "after one",
            "before two",
            "test two",
            "✓ root/flattened/is two",
            "after two",
            "before three",
            "test three",
            "X root/flattened/is two",
            "after three")
        assertAll(
            { assertEquals(1, allErrors.size) },
            { assertEquals(2, (allErrors[0] as MultipleFailuresError).failures.size) }
        )
    }

    private fun checkLog(tests: RootContextBuilder, vararg expected: String) =
        executeTests(tests).also {
            assertLogged(log, *expected)
        }
}