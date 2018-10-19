package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.JUnitTests
import org.junit.jupiter.api.Assertions.assertEquals

// Minutests are usually defined in a object.
// Extend JUnitTests to have them run by JUnit 5
object FirstMinutests : JUnitTests<Unit>({

    // define a test by calling test
    test("my first test") {
        // Minutest doesn't have any built-in assertions.
        // Here I'm using JUnit assertEquals
        assertEquals(2, 1 + 1)
    }
})
