package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.experimental.checkedAgainst
import com.oneeyedmen.minutest.experimental.loggedTo
import com.oneeyedmen.minutest.experimental.withTabsExpanded
import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import kotlin.test.assertEquals


class LoggingTests {


    @Test fun logging() {
        val log = mutableListOf<String>()

        val tests = junitTests<Unit>(loggedTo(log)) {

            test("top test") {}

            context("inner") {
                test("inner test") {}
            }
        }

        executeTests(tests)

        assertLogged(log.withTabsExpanded(2),
            "com.oneeyedmen.minutest.LoggingTests",
            "  top test",
            "  inner",
            "    inner test"
        )
    }

    @Test fun checking() {

        val expected = listOf(
            "com.oneeyedmen.minutest.LoggingTests",
            "  top test",
            "  inner",
            "    inner test")

        val tests = junitTests<Unit>(checkedAgainst { assertEquals(expected, it.withTabsExpanded(2))} ) {

            test("top test") {}

            context("inner") {
                test("inner test") {}
            }
        }

        executeTests(tests)
    }

    @Test fun `checking fails`() {

        val expected = emptyList<String>()

        val tests = junitTests<Unit>(checkedAgainst { assertEquals(expected, it)} ) {

            test("test") {}
        }

        assertThrows<AssertionError> {
            executeTests(tests)
        }
    }

    @Test fun `checks only after tests pass`() {

        val expected = emptyList<String>()

        val tests = junitTests<Unit>(checkedAgainst { assertEquals(expected, it)} ) {
            test("test") {
                throw IOException("deliberate")
            }
        }

        assertThrows<IOException> {
            executeTests(tests)
        }
    }
}
