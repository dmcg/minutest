package com.oneeyedmen.minutest

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.TestFactory


object ImmutableTests {

    @TestFactory fun `before and after`() = context<List<String>> {
        fixture { emptyList() }

        beforeF {
            assertTrue(isEmpty())
            this + "before"
        }

        beforeF {
            assertEquals(listOf("before"), this)
            this + "before too"
        }

        after {
            assertEquals(listOf("before", "before too", "during", "after"), this)
        }

        afterF {
            assertEquals(listOf("before", "before too", "during"), this)
            this + "after"
        }

        testF("before has been called") {
            assertEquals(listOf("before", "before too"), this)
            this + "during"
        }
    }
}