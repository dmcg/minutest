package dev.minutest.examples;

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertEquals

class MyFirstMinutests2 : JUnit5Minutests {

    private val calculator = Calculator()

    fun tests() = rootContext {

        test("addition") {
            assertEquals(0, calculator.currentValue)

            calculator.add(2)
            assertEquals(2, calculator.currentValue)
        }

        test("subtraction") {
            assertEquals(0, calculator.currentValue)

            calculator.add(2)
            assertEquals(2, calculator.currentValue)
        }
    }
}