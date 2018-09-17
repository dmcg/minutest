package com.oneeyedmen.minutest

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestFactory


object TransformTests {

    data class Fixture(
        var fruit: String,
        val log: MutableList<String> = mutableListOf()
    )

    @TestFactory fun `before and after`() = context<Fixture> {
        fixture { Fixture("banana") }

        before {
            assertTrue(log.isEmpty())
            log.add("before")
        }

        after {
            assertEquals(listOf("before", "during"), log)
        }

        test("before has been called") {
            assertEquals(listOf("before"), log)
            log.add("during")
        }

        context("also applies to contexts") {
            test("before is called") {
                assertEquals(listOf("before"), log)
                log.add("during")
            }
        }
    }

    @TestFactory fun `test transform`() = context<Fixture> {
        fixture { Fixture("banana") }

        modifyTests { test ->
            MinuTest(test.name) {
                this
            }
        }

        test("transform can ignore test") {
            fail("Shouldn't get here")
        }
    }
}