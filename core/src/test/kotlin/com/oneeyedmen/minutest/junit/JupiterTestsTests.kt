package com.oneeyedmen.minutest.junit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.*


object JupiterTestsWithPlainFixture : JupiterTests<String>() {

    override val tests = context {
        fixture { "banana" }

        test("test") {
            assertEquals("banana", this)
        }
    }
}

object JupiterTestsWithGenericFixture : JupiterTests<Stack<String>>() {

    override val tests = context {
        fixture { Stack() }

        test("test") {
            assertTrue(this.isEmpty())
        }
    }
}

object JupiterTestsWithNullableFixture : JupiterTests<String?>(fixtureIsNullable = true) {

    override val tests = context {
        fixture { "banana" }

        test("test") {
            val copy: String = this ?: "kumquat"
            assertEquals("banana", copy)
        }
    }
}

