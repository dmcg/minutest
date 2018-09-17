package com.oneeyedmen.minutest

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import java.util.*


object ExampleTests {

    // In the simplest case, make the fixture the thing that you are testing
    @TestFactory fun `stack is our fixture`() = context<Stack<String>> {

        // define the fixture for enclosed scopes
        fixture { Stack() }

        context("an empty stack") {

            test("is empty") {
                assertEquals(0, size) // note that the fixture is 'this'
                assertThrows<EmptyStackException> { peek() }
            }

            test("can have an item pushed") {
                push("one")
                assertEquals("one", peek())
                assertEquals(1, size)
            }
        }

        context("a stack with one item") {

            // we can modify the outer fixture
            modifyFixture { push("one") }

            test("is not empty") {
                assertEquals(1, size)
                assertEquals("one", peek())
            }

            test("removes and returns item on pop") {
                assertEquals("one", pop())
                assertEquals(0, size)
            }
        }
    }
}