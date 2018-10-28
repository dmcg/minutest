package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.TestFactory


object NullableFixtureTests {

    @TestFactory fun nullableFixture() = junitTests<Unit> {

        derivedContext<String?>("with null fixture") {
            fixture { null }
            test("fixture is null") {
                assertNull(this)
            }
        }

        derivedContext<String>("with non-null fixture") {
            fixture { "banana" }
            test("fixture is not null") {
                val copy: String = this ?: "was null"
                assertEquals("banana", copy)

            }
        }
    }
}