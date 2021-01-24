package dev.minutest

import dev.minutest.junit.JUnit5Minutests
import org.junit.jupiter.api.Assertions.assertEquals


class ImmutableTests : JUnit5Minutests {

    fun tests() = rootContext<List<String>> {
        fixture { emptyList() }

        after {
            assertEquals(listOf("during"), this)
        }

        test2_("before has been called") {
            assertEquals(emptyList<String>(), it)
            it + "during"
        }
    }
}