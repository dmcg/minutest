package dev.minutest.examples;

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertEquals

// Mix-in JUnit5Minutests to run Minutests with JUnit 5 (JUnit 4 support is also available)
class MyFirstMinutests : JUnit5Minutests {

    // tests are grouped in a context
    fun tests() = rootContext<Calculator> {

        // We need to tell Minutest how to build the fixture
        fixture { Calculator() }

        // define a test with a test block
        test("addition") {
            // inside tests, the fixture is `this`
            this.add(2)
            assertEquals(2, currentValue) // you can leave off the `this`
        }

        // each new test gets its own new fixture
        test("subtraction") {
            subtract(2)
            assertEquals(-2, currentValue)
        }
    }
}