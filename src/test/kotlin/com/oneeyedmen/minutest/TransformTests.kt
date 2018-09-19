package com.oneeyedmen.minutest

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.TestFactory


object TransformTests {

    @TestFactory fun `before and after`() = context<MutableList<String>> {
        fixture { mutableListOf() }

        // befores in order
        before {
            add("before")
        }

        before {
            add("before too")
        }

        // afters in reverse order
        after {
            assertEquals(listOf("before", "before too", "during", "after"), this)
        }

        after {
            assertEquals(listOf("before", "before too", "during"), this)
            add("after")
        }

        test("before has been called") {
            assertEquals(listOf("before", "before too"), this)
            add("during")
        }

        context("also applies to sub-contexts") {
            test("before is called") {
                assertEquals(listOf("before", "before too"), this)
                add("during")
            }
        }
    }

    @TestFactory fun `test transform`() = context<Unit> {

        modifyTests { test ->
            MinuTest(test.name) {}
        }

        test("transform can ignore test") {
            fail("Shouldn't get here")
        }
    }
}