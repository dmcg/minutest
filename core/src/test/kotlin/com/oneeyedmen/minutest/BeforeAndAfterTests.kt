package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.toTestFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.fail
import java.io.IOException


class BeforeAndAfterTests {

    // this is a very special case for testing testing - don't do this normally
    val log = mutableListOf<String>()
    private lateinit var expectedLog: List<String>

    @AfterEach fun checkLog() {
        assertEquals(expectedLog, log)
    }

    @TestFactory fun `plain before and after`() = rootContext<MutableList<String>> {

        expectedLog = listOf("before 1", "before 2", "test", "after 1", "after 2")

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
    }.toTestFactory()

    @TestFactory fun `nested before and after`() = rootContext<MutableList<String>> {

        expectedLog = listOf("outer before 1", "inner before", "inner fixture", "test", "inner after", "outer after")

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
    }.toTestFactory()

    @Test fun `after run on test failure`() {

        val test = rootContext<MutableList<String>> {
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
        checkItems(executeTests(test), { it.message == "in test" })
        expectedLog = listOf("test", "after")
    }

    @Test fun `after is run if before fails`() {

        val test = rootContext<MutableList<String>> {
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

        checkItems(executeTests(test), { it is IOException })
        expectedLog = listOf("before", "after")
    }

    @Test fun `afters are run with the last successful before fixture`() {

        // use an immutable fixture to prove the point
        val test = rootContext<List<String>> {
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
        checkItems(executeTests(test), { it.message == "in inner fixture" })
        expectedLog = listOf("top", "outer", "inner", "after outer")
    }

    @Test fun `afters abort if they throw`() {

        val test = rootContext<Unit> {

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

        checkItems(executeTests(test), { it.message == "in after 2" })
        expectedLog = listOf("test", "after 1", "after 2")
    }

    @Test fun `fails with the last exception`() {

        // use an immutable fixture to prove the point
        val test = rootContext<Unit> {

            test_("test") {
                log.add("test")
                throw Exception("in test")
            }

            after {
                throw Exception("in after")
            }

        }

        checkItems(executeTests(test), { it.message == "in after" })
        expectedLog = listOf("test")
    }
}