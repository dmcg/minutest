package dev.minutest.experimental

import dev.minutest.executeTests
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.opentest4j.AssertionFailedError


class CheckingTests {

    @Test fun checking() {

        val tests = rootContext {

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

        assertTrue(executeTests(tests).isEmpty())
    }

    @Test fun `when checking fails`() {

        val tests = rootContext {
            checkedAgainst(
                emptyList(),
                checker = ::assertEquals
            )
            test("test") {}
        }

        assertEquals(
            AssertionFailedError::class.java,
            executeTests(tests).single()::class.java
        )
    }
}
