package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.JUnit5Minutests
import com.oneeyedmen.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.*

class SimpleStackExampleTests : JUnit5Minutests {

    // The fixture type is the generic type of the test, here Stack<String>
    override val tests = rootContext<Stack<String>> {

        // The fixture block tells Minutest how to create an instance of the fixture.
        // Minutest will call it once for every test.
        fixture {
            Stack()
        }

        test("add an item") {
            // In a test, 'this' is the fixture created above
            assertTrue(this.isEmpty())

            this.add("item")
            assertFalse(this.isEmpty())
        }

        // another test will use a new fixture instance
        test("fixture is fresh") {
            // you can also access the fixture as 'fixture' if it reads nicer
            assertTrue(fixture.isEmpty())

            // or use the implicit 'this'
            assertFalse(isNotEmpty())
        }
    }
}