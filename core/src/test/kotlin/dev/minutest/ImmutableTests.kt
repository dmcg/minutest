package dev.minutest

import dev.minutest.junit.JUnit5Minutests
import org.junit.jupiter.api.Assertions.assertEquals


class ImmutableTests : JUnit5Minutests {

    fun tests() = rootContext<List<String>> {
        given { emptyList() }

        afterEach {
            assertEquals(listOf("during"), it)
        }

        test2_("before has been called") {
            assertEquals(emptyList<String>(), it)
            it + "during"
        }
    }
}