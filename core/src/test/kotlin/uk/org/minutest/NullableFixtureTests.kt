package uk.org.minutest

import org.junit.jupiter.api.Assertions.assertNull
import uk.org.minutest.junit.JUnit5Minutests


class NullableFixtureTests : JUnit5Minutests {

    override val tests = rootContext<String?> {
        fixture { null }
        test("fixture is null") {
            assertNull(this)
        }
    }
}