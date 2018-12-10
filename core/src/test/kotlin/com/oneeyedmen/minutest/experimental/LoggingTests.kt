package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.assertLogged
import com.oneeyedmen.minutest.executeTests
import com.oneeyedmen.minutest.rootContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import kotlin.test.assertEquals


class LoggingTests {


    @Test fun logging() {
        val log = mutableListOf<String>()

        val tests = rootContext<Unit>(loggedTo(log)) {

            test("top test") {}

            context("inner") {
                test("inner test") {}
            }
        }

        executeTests(tests)

        assertLogged(log.withTabsExpanded(2),
            "root",
            "  top test",
            "  inner",
            "    inner test"
        )
    }

    @Test fun checking() {

        val expected = listOf(
            "root",
            "  top test",
            "  inner",
            "    inner test")

        val tests = rootContext<Unit>(
            checkedAgainst { assertEquals(expected, it.withTabsExpanded(2))}
        ) {

            test("top test") {}

            context("inner") {
                test("inner test") {}
            }
        }

        executeTests(tests)
    }

    @Test fun `checking fails`() {

        val expected = emptyList<String>()

        val tests = rootContext<Unit>(
            checkedAgainst { assertEquals(expected, it)})
        {
            test("test") {}
        }

        assertThrows<AssertionError> {
            executeTests(tests)
        }
    }

    @Test fun `checks only after tests pass`() {

        val expected = emptyList<String>()

        val tests = rootContext<Unit>(
            checkedAgainst { assertEquals(expected, it)}) {
            test("test") {
                throw IOException("deliberate")
            }
        }

        assertThrows<IOException> {
            executeTests(tests)
        }
    }
}
