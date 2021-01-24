package dev.minutest.examples.fixtures

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test2
import org.junit.jupiter.api.Assertions.assertEquals

class NoFixtureExampleTests : JUnit5Minutests {

    fun tests() = rootContext {

        context("addition") {
            test2("positive + positive") {
                assertEquals(4, 3 + 1)
            }
            test2("positive + negative") {
                assertEquals(2, 3 + -1)
            }
        }
        context("subtraction") {
            test2("positive - positive") {
                assertEquals(2, 3 - 1)
            }
            test2("positive - negative") {
                assertEquals(4, 3 - -1)
            }
        }
    }
}