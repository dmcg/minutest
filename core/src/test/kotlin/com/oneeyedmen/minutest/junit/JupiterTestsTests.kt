package com.oneeyedmen.minutest.junit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.*


class JupiterTestsWithPlainFixture : JupiterTests {

    override val tests = context<String> {
        fixture { "banana" }

        test("test") {
            assertEquals("banana", this)
        }
    }
}

class JupiterTestsWithGenericFixture : JupiterTests {

    override val tests = context<Stack<String>> {
        fixture { Stack() }

        test("test") {
            assertTrue(this.isEmpty())
        }
    }
}

class JupiterTestsWithNullableFixture : JupiterTests {

    override val tests = context<String?> {
        fixture { "banana" }

        test("test") {
            val copy: String = this ?: "kumquat"
            assertEquals("banana", copy)
        }
    }
}

class JupiterTestsWithSuppliedFixture : JupiterTests {

    override val tests = context("banana") {

        test("test") {
            assertEquals("banana", this)
        }
    }
}

