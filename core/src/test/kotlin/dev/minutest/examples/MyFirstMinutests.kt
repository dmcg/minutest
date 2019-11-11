package dev.minutest.examples;

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertEquals

// Mix-in JUnit5Minutests to run Minutests with JUnit 5
class MyFirstMinutests : JUnit5Minutests {

    private val calculator = Calculator()

    // tests are grouped in a context
    fun tests() = rootContext {

        // define a test with a test block
        test("addition") {
            assertEquals(0, calculator.currentValue)

            calculator.add(2)
            assertEquals(2, calculator.currentValue)
        }
    }
}