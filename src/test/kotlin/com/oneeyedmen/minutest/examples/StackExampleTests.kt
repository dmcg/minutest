package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import java.util.*


// Minutests are usually defined in a object
object StackExampleTests {

    @TestFactory // junitTests() returns a stream of tests. JUnit 5 will run them for us.
    fun `when new`() = junitTests<Stack<String>> {

        // in this case the test fixture is just the stack we are testing
        fixture { Stack() }

        // define tests like this
        test("is empty") {
            // In a test, 'this' is our fixture, the stack in this case
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

            test("is not empty") {
                assertFalse(isEmpty())
            }

            test("returns the element when popped and is empty") {
                assertEquals("one", pop());
                assertTrue(isEmpty());
            }

            test("returns the element when peeked but remains not empty") {
                assertEquals("one", peek());
                assertFalse(isEmpty());
            }
        }
    }
}

