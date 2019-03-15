package dev.minutest.experimental

import dev.minutest.executeTests
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


class CheckingTests {

    @Test fun checking() {

        val tests = rootContext<Unit> {

            checkedAgainst(
                listOf(
                    "root",
                    "  top test",
                    "  inner",
                    "    inner test"),
                checker = ::assertEquals
            )

            test("top test") {}

            context("inner") {
                test("inner test") {}
            }
        }

        executeTests(tests)
    }

    @Test fun `checking fails`() {

        val tests = rootContext<Unit> {
            checkedAgainst(emptyList(), checker = ::assertEquals)
            test("test") {}
        }

        assertThrows<AssertionError> {
            executeTests(tests)
        }
    }
}
