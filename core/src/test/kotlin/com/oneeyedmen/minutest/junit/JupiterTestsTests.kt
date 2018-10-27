package com.oneeyedmen.minutest.junit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.*


object JupiterTestsWithPlainFixture : JupiterTests {

    override val tests = context<String> {
        fixture { "banana" }

        test("test") {
            assertEquals("banana", this)
        }
    }
}

object JupiterTestsWithGenericFixture : JupiterTests {

    override val tests = context<Stack<String>> {
        fixture { Stack() }

        test("test") {
            assertTrue(this.isEmpty())
        }
    }
}

object JupiterTestsWithNullableFixture : JupiterTests {

    override val tests = context<String?> {
        fixture { "banana" }

        test("test") {
            val copy: String = this ?: "kumquat"
            assertEquals("banana", copy)
        }
    }
}

object InlineJupiterTestsTests : InlineJupiterTests<String>( {
    fixture { "banana" }

    test("test") {
        assertEquals("banana", this)
    }
})

