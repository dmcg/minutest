package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestFactory


class DynamicTests {

    data class Fixture(
        var fruit: String,
        val log: MutableList<String> = mutableListOf()
    )

    @TestFactory fun `dynamic generation`() = junitTests<Fixture> {
        fixture { Fixture("banana") }

        context("same fixture for each") {
            (1..3).forEach { i ->
                test("test for $i") {}
            }
        }

        context("modify fixture for each test") {
            (1..3).forEach { i ->
                context("banana count $i") {
                    deriveFixture { Fixture("$i ${fruit}") }
                    test("test for $i") {
                        assertEquals("$i banana", fruit)
                    }
                }
            }
        }
    }
}