package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.IOException
import java.util.stream.Stream
import kotlin.streams.asSequence


class AfterExceptionsTests {

    private val log = mutableListOf<String>()

    @Test fun `if before throws, after is called with parent fixture`() {

        // This is a bit counter-intuitive, but saves trying to work our the last replace or before that succeeded

        val tests = junitTests<String> {
            fixture { "initial" }

            context("to make sure there is a fixture") {
                replaceFixture {
                    log.add("replace = $this")
                    "replaced"
                }
                before {
                    log.add("before = $this")
                    throw IOException("in before")
                }
                after {
                    log.add("after = $this")
                }
                test("not reached") {
                    log.add("test = $this")
                }
            }
        }
        assertThrows<IOException>("in before") {
            executeFirstTestFromContainer(tests)
        }
        assertEquals(listOf("replace = initial", "before = replaced", "after = initial"), log)
    }

    @Test fun `if before throws and no parent fixture throws IllegalStateException and can't invoke after`() {
        val test = junitTests<String> {
            fixture { "initial" }

            before {
                log.add("before = $this")
                throw IOException("in before")
            }
            after {
                log.add("after = $this")
            }
            test("not reached") {
                log.add("test = $this")
            }
        }
        assertThrows<IllegalStateException>("You need to set a fixture by calling fixture(...)") {
            executeTest(test)
        }
        assertEquals(listOf("before = initial"), log)
    }

    @Test fun `if test throws, after is called with fixture`() {
        val test = junitTests<String> {
            fixture { "initial" }
            before {
                log.add("before = $this")
            }
            after {
                log.add("after = $this")
            }
            test("throws") {
                log.add("test = $this")
                throw IOException("in test")
            }
        }

        assertThrows<IOException>("in test") {
            executeTest(test)
        }
        assertEquals(listOf("before = initial", "test = initial", "after = initial"), log)
    }
}

private fun executeFirstTestFromContainer(tests: Stream<out DynamicNode>) {
    val children = (tests.asSequence().first() as DynamicContainer).children.asSequence()
    (children.first() as DynamicTest).executable.execute()
}

private fun executeTest(tests: Stream<out DynamicNode>) {
    (tests.asSequence().first() as DynamicTest).executable.execute()
}