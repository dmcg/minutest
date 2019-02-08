package dev.minutest.experimental

import dev.minutest.executeTests
import dev.minutest.rootContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals


class CheckingTests {

    @Test fun checking() {

        val expected = listOf(
            "root",
            "  top test",
            "  inner",
            "    inner test")

        val tests = rootContext<Unit>(
            checkedAgainst { assertEquals(expected, it)}
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
}
