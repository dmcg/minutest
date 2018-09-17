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

        afterF {
            assertEquals(listOf("before", "during"), this)
            this
        }

        testF("before has been called") {
            assertEquals(listOf("before"), this)
            this + "during"
        }
    }
}