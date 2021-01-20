package dev.minutest

import dev.minutest.testing.runTests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.io.IOException


class BeforeAndAfterTests {

    val log = mutableListOf<String>()

    @Test
    fun `plain before and after`() {
        val tests = rootContext<MutableList<String>> {
            fixture { log }

            before {
                assertEquals(emptyList<String>(), this)
                add("before 1")
            }

            before {
                assertEquals(listOf("before 1"), this)
                add("before 2")
            }

            after {
                assertEquals(listOf("before 1", "before 2", "test"), this)
                add("after 1")
            }

            after {
                assertEquals(listOf("before 1", "before 2", "test", "after 1"), this)
                add("after 2")
            }

            test("test") {
                assertEquals(listOf("before 1", "before 2"), this)
                add("test")
            }
        }
        expect(
            whenRunning = tests,
            expectedLog = listOf("before 1", "before 2", "test", "after 1", "after 2"),
        )
    }

    @Test
    fun before_() {
        val tests = rootContext<List<String>> {
            fixture { emptyList() }

            before_ {
                assertEquals(emptyList<String>(), this)
                this + "before 1"
            }

            before_ {
                assertEquals(listOf("before 1"), this)
                this + "before 2"
            }

            after {
                assertEquals(listOf("before 1", "before 2", "test"), this)
                log.addAll(fixture)
            }

            test_("test") {
                assertEquals(listOf("before 1", "before 2"), this)
                this + "test"
            }
        }
        expect(
            whenRunning = tests,
            expectedLog = listOf("before 1", "before 2", "test"),
        )
    }

    @Test
    fun `nested before and after`() {
        val tests = rootContext<MutableList<String>> {
            fixture { log }

            before {
                assertEquals(emptyList<String>(), this)
                add("outer before 1")
            }

            after {
                assertEquals(listOf("outer before 1", "inner before", "inner fixture", "test", "inner after"), this)
                add("outer after")
            }

            context("inner") {
                before {
                    assertEquals(listOf("outer before 1"), this)
                    add("inner before")
                }

                modifyFixture {
                    assertEquals(listOf("outer before 1", "inner before"), this)
                    add("inner fixture")
                }

                after {
                    assertEquals(listOf("outer before 1", "inner before", "inner fixture", "test"), this)
                    add("inner after")
                }

                test("test") {
                    assertEquals(listOf("outer before 1", "inner before", "inner fixture"), this)
                    add("test")
                }
            }
        }
        expect(
            whenRunning = tests,
            expectedLog = listOf(
                "outer before 1", "inner before", "inner fixture",
                "test", "inner after", "outer after"
            ),
        )
    }

    @Test
    fun `after run on test failure`() {
        val tests = rootContext<MutableList<String>> {
            fixture { log }

            after {
                assertEquals(listOf("test"), this)
                add("after")
            }

            test("test") {
                add("test")
                throw Exception("in test")
            }
        }
        expect(
            whenRunning = tests,
            expectedLog = listOf("test", "after"),
            { it.message == "in test" }
        )
    }

    @Test
    fun `after is run if before fails`() {
        val tests = rootContext<MutableList<String>> {
            fixture { log }

            before {
                add("before")
                throw IOException("deliberate")
            }

            after {
                assertEquals(listOf("before"), this)
                add("after")
            }

            test("not run") {
                add("test")
            }
        }
        expect(
            whenRunning = tests,
            expectedLog = listOf("before", "after"),
            { it is IOException }
        )
    }

    @Test
    fun `afters are run with the last successful before fixture`() {

        // use an immutable fixture to prove the point
        val tests = rootContext<List<String>> {
            fixture {
                log.add("top")
                listOf("top")
            }

            context("outer") {

                deriveFixture {
                    log.add("outer")
                    this.plus("outer")
                }

                context("inner") {
                    fixture {
                        log.add("inner")
                        throw Exception("in inner fixture")
                    }

                    test("wont run") {
                        log.add("test")
                    }

                    // this isn't run because the fixture call didn't complete. TODO - Not sure whether it should
                    after {
                        log.add("after inner")
                        fail("doesn't get here")
                    }
                }

                after {
                    log.add("after outer")
                    assertEquals(listOf("top", "outer"), this)
                }
            }
        }
        expect(
            whenRunning = tests,
            expectedLog = listOf("top", "outer", "inner", "after outer"),
            { it.message == "in inner fixture" }
        )
    }

    @Test
    fun `afters abort if they throw`() {
        val tests = rootContext {

            test("test") {
                log.add("test")
            }

            after {
                log.add("after 1")
            }

            after {
                log.add("after 2")
                throw Exception("in after 2")
            }

            after {
                log.add("after 3")
            }
        }

        expect(
            whenRunning = tests,
            expectedLog = listOf("test", "after 1", "after 2"),
            { it.message == "in after 2" }
        )
    }

    @Test
    fun `fails with the last exception`() {
        // use an immutable fixture to prove the point
        val tests = rootContext {

            test_("test") {
                log.add("test")
                throw Exception("in test")
            }

            after {
                throw Exception("in after")
            }
        }

        expect(
            whenRunning = tests,
            expectedLog = listOf("test"),
            { it.message == "in after" }
        )
    }

    private fun expect(
        whenRunning: RootContextBuilder,
        expectedLog: List<String>,
        vararg exceptionMatchers: (Throwable) -> Boolean
    ) {
        val exceptions = runTests(whenRunning)
        checkItems(exceptions, *exceptionMatchers)
        assertEquals(expectedLog, log)
    }
}