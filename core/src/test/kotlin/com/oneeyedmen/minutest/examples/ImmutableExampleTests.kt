package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.JupiterTests
import org.junit.jupiter.api.Assertions.assertEquals


object ImmutableExampleTests : JupiterTests<List<String>>() {

    override val tests = context {
        // If you like this FP stuff, you may want to test an immutable fixture.
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

        // there are also before_ and after_ which return new fixtures
    }
}