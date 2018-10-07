package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.TestFactory


object ImmutableTests {

    @TestFactory fun `before and after`() = junitTests<List<String>> {
        fixture { emptyList() }

        before_ {
            assertTrue(isEmpty())
            this + "before"
        }

        before_ {
            assertEquals(listOf("before"), this)
            this + "before too"
        }

        after_ {
            assertEquals(listOf("before", "before too", "during"), this)
            this + "after"
        }

        after {
            assertEquals(listOf("before", "before too", "during", "after"), this)
        }

        test_("before has been called") {
            assertEquals(listOf("before", "before too"), this)
            this + "during"
        }
    }
}