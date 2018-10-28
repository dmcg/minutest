package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.JupiterTests
import org.junit.jupiter.api.Assertions.assertEquals


object ImmutableExampleTests : JupiterTests {

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
            println("in after")
            assertEquals("item", first())
        }
    }
}