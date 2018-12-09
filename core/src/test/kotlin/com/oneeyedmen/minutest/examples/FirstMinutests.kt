package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.JUnit5Minutests
import com.oneeyedmen.minutest.junit.context
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals

// Implement JUnit5Minutests to run Minutests with JUnit 5
class FirstMinutests : JUnit5Minutests {

    // tests are grouped in a context
    override val tests = context<Unit> {

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