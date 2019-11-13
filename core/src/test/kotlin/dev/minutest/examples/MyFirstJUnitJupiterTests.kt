package dev.minutest.examples;

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class MyFirstJUnitJupiterTests {

    private val calculator = Calculator()

    @Test
    fun addition() {
        calculator.add(2)
        assertEquals(2, calculator.currentValue)
    }

    @Test
    fun subtraction() {
        calculator.subtract(2)
        assertEquals(-2, calculator.currentValue)
    }
}