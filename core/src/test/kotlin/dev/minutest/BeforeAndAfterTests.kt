package dev.minutest

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.io.IOException


class BeforeAndAfterTests {

    @Test
    fun `plain before and after`() {
        runWithLog(
            expectedLog = listOf("before 1", "before 2", "test", "after 1", "after 2")
        ) { log ->
            rootContext<MutableList<String>> {
                given { log }

                beforeEach {
                    assertEquals(emptyList<String>(), it)
                    add("before 1")
                }

                beforeEach {
                    assertEquals(listOf("before 1"), it)
                    add("before 2")
                }

                afterEach {
                    assertEquals(listOf("before 1", "before 2", "test"), it)
                    it.add("after 1")
                }

                afterEach {
                    assertEquals(listOf("before 1", "before 2", "test", "after 1"), it)
                    it.add("after 2")
                }

                test("test") {
                    assertEquals(listOf("before 1", "before 2"), it)
                    add("test")
                }
            }
        }.withNoExceptions()
    }

    @Test
    fun before_() {
        runWithLog(
            expectedLog = listOf("before 1", "before 2", "test"),
        ) { log ->
            rootContext<List<String>> {
                given { emptyList() }

                beforeEach_ {
                    assertEquals(emptyList<String>(), it)
                    it + "before 1"
                }

                beforeEach_ {
                    assertEquals(listOf("before 1"), it)
                    it + "before 2"
                }

                afterEach {
                    assertEquals(listOf("before 1", "before 2", "test"), this)
                    log.addAll(it)
                }

                test_("test") {
                    assertEquals(listOf("before 1", "before 2"), it)
                    it + "test"
                }
            }
        }.withNoExceptions()
    }

    @Test
    fun `nested before and after`() {
        runWithLog(
            expectedLog = listOf(
                "outer before 1", "inner before", "inner fixture",
                "test", "inner after", "outer after"
            )
        ) { log ->
            rootContext<MutableList<String>> {
                given { log }

                beforeEach {
                    assertEquals(emptyList<String>(), it)
                    it.add("outer before 1")
                }

                afterEach {
                    assertEquals(listOf("outer before 1", "inner before", "inner fixture", "test", "inner after"), it)
                    it.add("outer after")
                }

                context("inner") {
                    beforeEach {
                        assertEquals(listOf("outer before 1"), it)
                        it.add("inner before")
                    }

                    beforeEach {
                        assertEquals(listOf("outer before 1", "inner before"), this)
                        it.add("inner fixture")
                    }

                    afterEach {
                        assertEquals(listOf("outer before 1", "inner before", "inner fixture", "test"), it)
                        it.add("inner after")
                    }

                    test("test") {
                        assertEquals(listOf("outer before 1", "inner before", "inner fixture"), it)
                        it.add("test")
                    }
                }
            }
        }
    }

    @Test
    fun `after run on test failure`() {
        runWithLog(
            expectedLog = listOf("test", "after")
        ) { log ->
            rootContext<MutableList<String>> {
                given { log }

                afterEach {
                    assertEquals(listOf("test"), it)
                    add("after")
                }

                test("test") {
                    it.add("test")
                    throw Exception("in test")
                }
            }
        }.hasExceptionsMatching(
            { it.message == "in test" }
        )
    }

    @Test
    fun `after is run if before fails`() {
        runWithLog(
            expectedLog = listOf("before", "after"),
        ) { log ->
            rootContext<MutableList<String>> {
                given { log }

                beforeEach {
                    it.add("before")
                    throw IOException("deliberate")
                }

                afterEach {
                    assertEquals(listOf("before"), it)
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
        runWithLog(
            expectedLog = listOf("top", "outer", "inner", "after outer"),
        ) { log ->
            rootContext<List<String>> {
                given {
                    log.add("top")
                    listOf("top")
                }

                context("outer") {

                    given_ { parentFixture ->
                        log.add("outer")
                        parentFixture.plus("outer")
                    }

                    context("inner") {
                        given {
                            log.add("inner")
                            throw Exception("in inner fixture")
                        }

                        test("wont run") {
                            fail("doesn't get here")
                        }

                        // this isn't run because the fixture call didn't complete
                        afterEach {
                            log.add("after inner")
                            fail("doesn't get here")
                        }
                    }

                    afterEach {
                        log.add("after outer")
                        assertEquals(listOf("top", "outer"), it)
                    }
                }
            }
        }.hasExceptionsMatching(
            { it.message == "in inner fixture" }
        )
    }

    @Test
    fun `afters abort if they throw`() {
        runWithLog(
            expectedLog = listOf("test", "after 1", "after 2"),
        ) { log ->
            rootContext {

                test("test") {
                    log.add("test")
                }

                afterEach {
                    log.add("after 1")
                }

                afterEach {
                    log.add("after 2")
                    throw Exception("in after 2")
                }

                afterEach {
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
        runWithLog(
            expectedLog = listOf("test"),
        ) { log ->
            rootContext {

                test_("test") {
                    log.add("test")
                    throw Exception("in test")
                }

                afterEach {
                    throw Exception("in after")
                }
            }
        }.hasExceptionsMatching(
            { it.message == "in after" }
        )
    }
}
