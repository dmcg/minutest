package uk.org.minutest

import org.junit.jupiter.api.Assertions.assertEquals
import uk.org.minutest.junit.JUnit5Minutests


class ImmutableTests : JUnit5Minutests {

    override val tests = rootContext<List<String>> {
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