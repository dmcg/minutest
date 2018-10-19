package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.JUnitTests
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.*

// The fixture type is the generic type of the test, here Stack<String>
object SimpleStackExampleTests : JUnitTests<Stack<String>>({

    // Instead of defining the fixture as a field of the test like JUnit,
    // in Minutest you call 'fixture' to initialise it for every test.
    fixture { Stack() }

    // In a test, 'this' is the fixture created above
    test("run first") {
        assertTrue(this.isEmpty())

        // you can leave out 'this'
        add("item")
        assertFalse(isEmpty())
    }

    // another test will use a new fixture instance
    test("run second") {
        assertTrue(this.isEmpty())
    }
})


