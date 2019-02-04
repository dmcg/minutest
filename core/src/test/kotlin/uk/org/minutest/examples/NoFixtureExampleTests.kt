package uk.org.minutest.examples

import org.junit.jupiter.api.Assertions.assertEquals
import uk.org.minutest.junit.JUnit5Minutests
import uk.org.minutest.rootContext

class NoFixtureExampleTests : JUnit5Minutests {

    override val tests = rootContext<Unit> {

        context("addition") {
            test("positive + positive") {
                assertEquals(4, 3 + 1)
            }
            test("positive + negative") {
                assertEquals(2, 3 + -1)
            }
        }
        context("subtraction") {
            test("positive - positive") {
                assertEquals(2, 3 - 1)
            }
            test("positive - negative") {
                assertEquals(4, 3 - -1)
            }
        }
    }
}