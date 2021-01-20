package dev.minutest

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.io.IOException


class BeforeAndAfterTests {

    @Test
    fun `plain before and after`() {
        check(
            expectedLog = listOf("before 1", "before 2", "test", "after 1", "after 2")
        ) { log ->
            rootContext<MutableList<String>> {
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
        }.withNoExceptions()
    }

    @Test
    fun before_() {
        check(
            expectedLog = listOf("before 1", "before 2", "test"),
        ) { log ->
            rootContext<List<String>> {
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
        }.withNoExceptions()
    }

    @Test
    fun `nested before and after`() {
        check(
            expectedLog = listOf(
                "outer before 1", "inner before", "inner fixture",
                "test", "inner after", "outer after"
            )
        ) { log ->
            rootContext<MutableList<String>> {
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
        }
    }

    @Test
    fun `after run on test failure`() {
        check(
            expectedLog = listOf("test", "after")
        ) { log ->
            rootContext<MutableList<String>> {
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
        }.hasExceptionsMatching(
            { it.message == "in test" }
        )
    }

    @Test
    fun `after is run if before fails`() {
        check(
            expectedLog = listOf("before", "after"),
        ) { log ->
            rootContext<MutableList<String>> {
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
        }.hasExceptionsMatching(
            { it is IOException }
        )
    }

    @Test
    fun `afters are run with the last successful before fixture`() {
        // use an immutable fixture to prove the point
        check(
            expectedLog = listOf("top", "outer", "inner", "after outer"),
        ) { log ->
            rootContext<List<String>> {
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
                            fail("doesn't get here")
                        }

                        // this isn't run because the fixture call didn't complete
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
        }.hasExceptionsMatching(
            { it.message == "in inner fixture" }
        )
    }

    @Test
    fun `afters abort if they throw`() {
        check(
            expectedLog = listOf("test", "after 1", "after 2"),
        ) { log ->
            rootContext {

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
        }.hasExceptionsMatching(
            { it.message == "in after 2" }
        )
    }

    @Test
    fun `fails with the last exception`() {
        // use an immutable fixture to prove the point
        check(
            expectedLog = listOf("test"),
        ) { log ->
            rootContext {

                test_("test") {
                    log.add("test")
                    throw Exception("in test")
                }

                after {
                    throw Exception("in after")
                }
            }
        }.hasExceptionsMatching(
            { it.message == "in after" }
        )
    }
}
