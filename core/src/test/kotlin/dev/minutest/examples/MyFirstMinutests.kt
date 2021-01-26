package dev.minutest.examples

import dev.minutest.given
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test
import org.junit.jupiter.api.Assertions.assertEquals

// Mix-in JUnit5Minutests to run Minutests with JUnit 5
//
// (JUnit 4 support is also available, see [JUnit4Minutests].)
class MyFirstMinutests : JUnit5Minutests {

    // tests are grouped in a context
    fun tests() = rootContext<Calculator> {

        // We need to tell Minutest how to build the fixture
        given { Calculator() }

        // define a test with a test block
        test("addition") {
            // inside tests, the fixture is `it`
            it.add(2)
            assertEquals(2, it.currentValue)
        }

        // each new test gets its own new fixture
        test("subtraction") { calculator ->
            subtract(2)
            assertEquals(-2, calculator.currentValue)
        }
    }
}