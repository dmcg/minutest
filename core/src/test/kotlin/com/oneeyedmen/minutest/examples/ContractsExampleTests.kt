package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.TestContext
import com.oneeyedmen.minutest.junit.JUnit5Minutests
import com.oneeyedmen.minutest.junit.context
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.*


// To run the same tests against different implementations, first define a TestContext extension function
// that defines the tests you want run.
fun TestContext<MutableCollection<String>>.behavesAsMutableCollection() {

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

    override val tests = context<MutableCollection<String>> {
        fixture {
            ArrayList()
        }

        behavesAsMutableCollection()
    }
}

// We can reuse the contract for different collections.
class LinkedListTests : JUnit5Minutests {

    override val tests = context<MutableCollection<String>> {
        fixture {
            LinkedList()
        }

        behavesAsMutableCollection()
    }
}