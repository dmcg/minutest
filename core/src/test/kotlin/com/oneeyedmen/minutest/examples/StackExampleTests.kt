package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.JupiterTests
import com.oneeyedmen.minutest.junit.context
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import java.util.EmptyStackException
import java.util.Stack

class StackExampleTests : JupiterTests {

    override val tests = context<Stack<String>> {

        // The tests in the root context run with this empty stack
        fixture {
            Stack()
        }

        test("is empty") {
            assertTrue(it.isEmpty())
        }

        test("throws EmptyStackException when popped") {
            assertThrows<EmptyStackException> { pop() }
        }

        test("throws EmptyStackException when peeked") {
            assertThrows<EmptyStackException> { peek() }
        }

        // nested a context
        context("after pushing an element") {

            // This context modifies the fixture from its parent -
            // the tests run with the single item stack.
            modifyFixture {
                parentFixture.push("one")
            }

            test("is not empty") {
                assertFalse(it.isEmpty())
            }

            test("returns the element when popped and is empty") {
                assertEquals("one", pop())
                assertTrue(it.isEmpty())
            }

            test("returns the element when peeked but remains not empty") {
                assertEquals("one", peek())
                assertFalse(it.isEmpty())
            }
        }
    }
}