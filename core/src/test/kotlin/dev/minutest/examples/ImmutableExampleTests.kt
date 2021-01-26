package dev.minutest.examples

import dev.minutest.afterEach
import dev.minutest.given
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test_
import org.junit.jupiter.api.Assertions.assertEquals


class ImmutableExampleTests : JUnit5Minutests {

    // If you like this FP stuff, you may want to test an immutable fixture.
    fun tests() = rootContext<List<String>> {

        // List<String> is immutable
        given { emptyList() }

        // test_ allows you to return the fixture
        test_("add an item and return the fixture") {
            val newList = it + "item"
            assertEquals("item", newList.first())
            newList
        }

        // which will be available for inspection in after
        afterEach {
            assertEquals("item", it.first())
        }
    }
}