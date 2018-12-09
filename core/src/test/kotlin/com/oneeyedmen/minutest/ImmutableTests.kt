package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.JUnit5Minutests
import com.oneeyedmen.minutest.junit.context
import org.junit.jupiter.api.Assertions.assertEquals


class ImmutableTests : JUnit5Minutests {

    override val tests = context<List<String>> {
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