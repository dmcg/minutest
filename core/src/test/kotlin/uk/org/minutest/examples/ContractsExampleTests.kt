package uk.org.minutest.examples

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import uk.org.minutest.ContextBuilder
import uk.org.minutest.junit.JUnit5Minutests
import uk.org.minutest.rootContext
import java.util.*


// To run the same tests against different implementations, first define a TestContext extension function
// that defines the tests you want run.
fun ContextBuilder<MutableCollection<String>>.behavesAsMutableCollection() {

    context("behaves as MutableCollection") {

        test("is empty when created") {
            assertTrue(isEmpty())
        }

        test("can add") {
            add("item")
            assertEquals("item", first())
        }
    }
}

// Now tests can supply the fixture and invoke the function to create the tests to verify the contract.
class ArrayListTests : JUnit5Minutests {

    override val tests = rootContext<MutableCollection<String>> {
        fixture {
            ArrayList()
        }

        behavesAsMutableCollection()
    }
}

// We can reuse the contract for different collections.
class LinkedListTests : JUnit5Minutests {

    override val tests = rootContext<MutableCollection<String>> {
        fixture {
            LinkedList()
        }

        behavesAsMutableCollection()
    }
}