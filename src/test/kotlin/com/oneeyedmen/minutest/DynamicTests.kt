package com.oneeyedmen.minutest

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestFactory


object DynamicTests {

    data class Fixture(
        var fruit: String,
        val log: MutableList<String> = mutableListOf()
    )

    @TestFactory fun `dynamic generation`() = context<Fixture> {
        fixture { Fixture("banana") }

        context("same fixture for each") {
            (1..3).forEach { i ->
                test("test for $i") {}
            }
        }

        context("modify fixture for each test") {
            (1..3).forEach { i ->
                context("banana count $i") {
                    replaceFixture { Fixture("$i $fruit") }
                    test("test for $i") {
                        assertEquals("$i banana", fruit)
                    }
                }
            }
        }
    }
}