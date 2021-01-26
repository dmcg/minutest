package dev.minutest.examples

import dev.minutest.given
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.*

class SimpleStackExampleTests : JUnit5Minutests {

    // The fixture type is the generic type of the test, here Stack<String>
    fun tests() = rootContext<Stack<String>> {

        // The fixture block tells Minutest how to create an instance of the fixture.
        // Minutest will call it once for every test.
        given {
            Stack()
        }

        test("add an item") {
            // In a test, 'it' is the fixture created above
            assertTrue(it.isEmpty())

            this.add("item")
            assertFalse(it.isEmpty())
        }

        // another test will use a new fixture instance
        test("fixture is fresh") { fixture ->
            // you can also access the fixture as 'fixture' if it reads nicer
            assertTrue(fixture.isEmpty())
            assertFalse(fixture.isNotEmpty())
        }
    }
}