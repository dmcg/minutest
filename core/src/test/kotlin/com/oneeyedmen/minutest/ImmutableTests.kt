package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestFactory


object ImmutableTests {

    @TestFactory fun `before and after`() = junitTests<List<String>> {
        fixture { emptyList() }

        after_ {
            assertEquals(listOf("during"), this)
            this + "after"
        }

        after {
            assertEquals(listOf("during", "after"), this)
        }

        test_("before has been called") {
            assertEquals(emptyList<String>(), this)
            this + "during"
        }
    }
}