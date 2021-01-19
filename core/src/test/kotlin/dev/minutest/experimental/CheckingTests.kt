package dev.minutest.experimental

import dev.minutest.executeTests
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


class CheckingTests {

    @Test fun checking() {

        val tests = rootContext {

            checkedAgainst(
                listOf(
                    "root",
                    "  top test",
                    "  inner",
                    "    inner test"
                ),
                logger = noSymbolsLogger(),
                checker = ::assertEquals
            )

            test("top test") {}

            context("inner") {
                test("inner test") {}
            }
        }

        assertTrue(executeTests(tests).isEmpty())
    }

    @Test fun `throws if checking fails`() {

        val tests = rootContext {
            checkedAgainst(emptyList(), checker = ::assertEquals)
            test("test") {}
        }

        assertThrows<AssertionError> {
            executeTests(tests)
        }
    }
}
