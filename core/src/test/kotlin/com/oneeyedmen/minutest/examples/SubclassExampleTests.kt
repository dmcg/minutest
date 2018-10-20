package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.TestContext
import com.oneeyedmen.minutest.junit.JupiterTests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.*


// To run the same tests against different implementations, first define a function
// taking the implementation and returning a TestContext
fun TestContext<MutableCollection<String>>.behavesAsMutableCollection(
    collectionName: String,
    factory: () -> MutableCollection<String>
) {
    context("$collectionName behaves as MutableCollection") {

        fixture { factory() }

        test("is empty") {
            assertTrue(isEmpty())
        }

        test("can add") {
            add("item")
            assertEquals("item", first())
        }
    }
}

// Now tests can invoke the function to define a context to be run

object ArrayListTests : JupiterTests<MutableCollection<String>>() {
    override val tests = context {
        behavesAsMutableCollection("ArrayList") { ArrayList() }
    }
}

object LinkedListTests : JupiterTests<MutableCollection<String>>() {
    override val tests = context {
        behavesAsMutableCollection("LinkedList") { LinkedList() }
    }
}