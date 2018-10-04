package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import java.util.*


object FixtureExampleTests {

    // If you have more state, make a separate fixture class
    class Fixture {
        val stack1 = Stack<String>()
        val stack2 = Stack<String>()
    }

    // and then use it in your tests
    @TestFactory fun `separate fixture class`() = junitTests<Fixture> {

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
}

private fun <E> Stack<E>.swapTop(otherStack: Stack<E>) {
    val myTop = pop()
    push(otherStack.pop())
    otherStack.push(myTop)
}
