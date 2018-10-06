package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import java.util.*

// Tests are usually defined in a object
object StackExampleTests {

    // junitTests() returns a stream of tests. JUnit 5 will run them for us.
    @TestFactory fun `a stack`() = junitTests<Stack<String>> {

        // in this case the test fixture is just the stack we are testing
        fixture { Stack() }

        // a context groups tests with the same fixture
        context("an empty stack") {

            // this context inherits the empty stack from its parent

            // define tests like this
            test("is empty") {
                // In a test, 'this' is our fixture, the stack in this case
                assertEquals(0, size)
                assertThrows<EmptyStackException> { peek() }
            }

            // .. other tests
        }

        // another context
        context("a stack with one item") {

            // this context modifies the fixture from its parent
            modifyFixture { push("one") }

            test("is not empty") {
                assertEquals(1, size)
                assertEquals("one", peek())
            }

            // .. other tests
        }
    }
}