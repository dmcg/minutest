package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import java.util.*


object StackExampleTests {

    // The @TestFactory annotation tells JUnit 5 that the call of junitTests will return tests that should be run
    @TestFactory fun `a stack`() = junitTests<Stack<String>> {

        // define the fixture for enclosed scopes
        fixture { Stack() }

        context("an empty stack") {

            test("is empty") {
                // In a test, 'this' is our fixture, the stack in this case
                assertEquals(0, size)
                assertThrows<EmptyStackException> { peek() }
            }

            // .. other tests
        }

        context("a stack with one item") {

            // we can modify the outer fixture
            modifyFixture { push("one") }

            test("is not empty") {
                assertEquals(1, size)
                assertEquals("one", peek())
            }

            // .. other tests
        }
    }
}