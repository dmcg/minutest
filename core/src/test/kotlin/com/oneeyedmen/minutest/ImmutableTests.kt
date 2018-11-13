package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestFactory


class ImmutableTests {

    @TestFactory fun `before and after`() = junitTests<List<String>> {
        fixture { emptyList() }

        after {
            assertEquals(listOf("during"), this)
        }

        test_("before has been called") {
            assertEquals(emptyList<String>(), this)
            this + "during"
        }
    }
}