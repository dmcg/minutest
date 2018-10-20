package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.JupiterTests
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import java.util.*

object StackExampleTests : JupiterTests<Stack<String>>() {

    override val tests = context {

        fixture { Stack() }

        // these tests run with an empty stack

        test("is empty") {
            assertTrue(this.isEmpty())
        }

        test("throws EmptyStackException when popped") {
            assertThrows<EmptyStackException> { pop() }
        }

        test("throws EmptyStackException when peeked") {
            assertThrows<EmptyStackException> { peek() }
        }

        // nested context
        context("after pushing an element") {

            // this context modifies the fixture from its parent
            modifyFixture { push("one") }

            // these tests run with the single item stack

            test("is not empty") {
                assertFalse(isEmpty())
            }

            test("returns the element when popped and is empty") {
                assertEquals("one", pop())
                assertTrue(isEmpty())
            }

            test("returns the element when peeked but remains not empty") {
                assertEquals("one", peek())
                assertFalse(isEmpty())
            }
        }
    }
}