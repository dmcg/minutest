package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.JUnit5Minutests
import com.oneeyedmen.minutest.rootContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import java.util.*

class StackExampleTests : JUnit5Minutests {

    override val tests = rootContext<Stack<String>> {

        // The tests in the root context run with this empty stack
        fixture {
            Stack()
        }

        test("is empty") {
            assertTrue(fixture.isEmpty())
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
                assertFalse(fixture.isEmpty())
            }

            test("returns the element when popped and is empty") {
                assertEquals("one", pop())
                assertTrue(fixture.isEmpty())
            }

            test("returns the element when peeked but remains not empty") {
                assertEquals("one", peek())
                assertFalse(fixture.isEmpty())
            }
        }
    }
}