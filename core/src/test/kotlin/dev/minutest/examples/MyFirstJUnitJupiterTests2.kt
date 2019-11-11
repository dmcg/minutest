package dev.minutest.examples;

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class MyFirstJUnitJupiterTests2 {

    // calculator is recreated for each test.
    private val calculator = Calculator()

    @Test
    fun addition() {
        assertEquals(0, calculator.currentValue)

        calculator.add(2)
        assertEquals(2, calculator.currentValue)
    }

    @Test
    fun subtraction() {
        assertEquals(0, calculator.currentValue)

        calculator.subtract(2)
        assertEquals(-2, calculator.currentValue)
    }
}