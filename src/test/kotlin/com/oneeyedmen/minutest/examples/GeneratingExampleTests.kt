package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.TestContext
import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import java.util.*


private typealias StringStack = Stack<String>

object GeneratingExampleTests {

    // We can define functions that return tests for later injection

    private fun TestContext<StringStack>.isEmpty(isEmpty: Boolean) =
        test("is " + (if (isEmpty) "" else "not ") + "empty") {
            assertEquals(isEmpty, size == 0)
            if (isEmpty)
                assertThrows<EmptyStackException> { peek() }
            else
                assertNotNull(peek())
        }

    private fun TestContext<StringStack>.canPush() =
        test("can push") {
            val initialSize = size
            val item = "*".repeat(initialSize + 1)
            push(item)
            assertEquals(item, peek())
            assertEquals(initialSize + 1, size)
        }

    private fun TestContext<StringStack>.canPop() =
        test("can pop") {
            val initialSize = size
            val top = peek()
            assertEquals(top, pop())
            assertEquals(initialSize - 1, size)
            if (size > 0)
                assertNotEquals(top, peek())
        }

    private fun TestContext<StringStack>.cantPop() =
        test("cant pop") {
            assertThrows<EmptyStackException> { pop() }
        }

    @TestFactory fun `invoke functions to inject tests`() = junitTests<StringStack> {

        fixture { StringStack() }

        context("an empty stack") {
            isEmpty(true)
            canPush()
            cantPop()
        }

        context("a stack with one item") {
            modifyFixture { push("one") }

            isEmpty(false)
            canPush()
            canPop()

            test("has the item on top") {
                assertEquals("one", peek())
            }
        }
    }

    @TestFactory fun `generate contexts to test with multiple values`() = junitTests<StringStack> {

        fun TestContext<StringStack>.canPop(canPop: Boolean) = if (canPop) canPop() else cantPop()

        (0..3).forEach { itemCount ->
            context("stack with $itemCount items") {

                fixture {
                    StringStack().apply {
                        (1..itemCount).forEach { add(it.toString()) }
                    }
                }

                isEmpty(itemCount == 0)
                canPush()
                canPop(itemCount > 0)
            }
        }
    }
}