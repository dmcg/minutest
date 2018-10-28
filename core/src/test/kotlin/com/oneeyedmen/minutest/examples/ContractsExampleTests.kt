package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.TestContext
import com.oneeyedmen.minutest.junit.InlineJupiterTests
import com.oneeyedmen.minutest.junit.JupiterTests
import com.oneeyedmen.minutest.junit.context
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.*


// To run the same tests against different implementations, first define a function
// taking the implementation and returning a TestContext
private fun TestContext<*, MutableCollection<String>>.behavesAsMutableCollection(
    collectionName: String,
    factory: () -> MutableCollection<String>
) {

    fixture { factory() }

    context("$collectionName behaves as MutableCollection") {

        test("is empty") {
            assertTrue(isEmpty())
        }

        test("can add") {
            add("item")
            assertEquals("item", first())
        }
    }
}

// Now tests can invoke the function to verify the contract in a context

object ArrayListTests : JupiterTests {

    override val tests = context<MutableCollection<String>> {
        behavesAsMutableCollection("ArrayList") { ArrayList() }
    }
}

// We can reuse the contract for different collections.

// Here we use the convenience InlineJupiterTests to reduce boilerplate
object LinkedListTests : InlineJupiterTests<MutableCollection<String>>({
    behavesAsMutableCollection("LinkedList") { LinkedList() }
})