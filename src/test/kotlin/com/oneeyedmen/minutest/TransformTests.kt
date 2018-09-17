package com.oneeyedmen.minutest

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.TestFactory


object TransformTests {

    data class Fixture(
        var fruit: String,
        val log: MutableList<String> = mutableListOf()
    )

    @TestFactory fun `before and after`() = context<Fixture> {
        fixture { Fixture("banana") }

        // befores in order
        before {
            log.add("before")
        }

        before {
            log.add("before too")
        }

        // afters in reverse order
        after {
            assertEquals(listOf("before", "before too", "during", "after"), log)
        }

        after {
            assertEquals(listOf("before", "before too", "during"), log)
            log.add("after")
        }

        test("before has been called") {
            assertEquals(listOf("before", "before too"), log)
            log.add("during")
        }

        context("also applies to sub-contexts") {
            test("before is called") {
                assertEquals(listOf("before", "before too"), log)
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