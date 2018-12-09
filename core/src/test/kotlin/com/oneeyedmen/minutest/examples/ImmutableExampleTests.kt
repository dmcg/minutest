package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.JUnit5Minutests
import com.oneeyedmen.minutest.junit.context
import org.junit.jupiter.api.Assertions.assertEquals


class ImmutableExampleTests : JUnit5Minutests {

    // If you like this FP stuff, you may want to test an immutable fixture.
    override val tests = context<List<String>> {

        // List<String> is immutable
        fixture { emptyList() }

        // test_ allows you to return the fixture
        test_("add an item and return the fixture") {
            val newList = this + "item"
            assertEquals("item", newList.first())
            newList
        }

        // which will be available for inspection in after
        after {
            assertEquals("item", first())
        }
    }
}