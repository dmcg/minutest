package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.JupiterTests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals

// Minutests are usually defined in a object.
// Extend JupiterTests to have them run by JUnit 5
object FirstMinutests : JupiterTests<Unit>() {

    // tests are grouped in a context
    override val tests = context {

        // define a test by calling test
        test("my first test") {
            // Minutest doesn't have any built-in assertions.
            // Here I'm using JUnit assertEquals
            assertEquals(2, 1 + 1)
        }

        // here is another one
        test("my second test") {
            assertNotEquals(42, 6 * 9)
        }
    }
}