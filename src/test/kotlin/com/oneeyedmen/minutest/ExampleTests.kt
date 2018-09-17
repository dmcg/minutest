package com.oneeyedmen.minutest

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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

    // If you have more state, make a separate fixture class

    class Fixture {
        val stack1 = Stack<String>()
        val stack2 = Stack<String>()
    }

    @TestFactory fun `separate fixture class`() = context<Fixture> {

        fixture { Fixture() }

        context("stacks with no items") {
            test("error to try to swap") {
                assertThrows<EmptyStackException> {
                    stack1.swapTop(stack2)
                }
            }
        }

        context("stacks with items") {
            modifyFixture {
                stack1.push("on 1")
                stack2.push("on 2")
            }

            test("swap top items") {
                stack1.swapTop(stack2)
                assertEquals("on 2", stack1.peek())
                assertEquals("on 1", stack2.peek())
            }
        }
    }

    // you can modify the fixture before, and inspect it after

    @TestFactory fun `before and after`() = context<Fixture> {
        fixture { Fixture() }

        before {
            stack1.push("item")
        }

        after {
            println("in after")
            assertTrue(stack2.isEmpty())
        }

        test("before was called") {
            assertEquals("item", stack1.peek())
        }
    }
}

private fun <E> Stack<E>.swapTop(otherStack: Stack<E>) {
    val myTop = pop()
    push(otherStack.pop())
    otherStack.push(myTop)
}
