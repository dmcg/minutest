package com.oneeyedmen.minutest

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import java.util.*


private typealias StringStack = Stack<String>

object GeneratingExampleTests {

    // We can define functions that return tests for later injection

    private fun TestContext<StringStack>.isEmpty(condition: Boolean): () -> MinuTest<StringStack> = {
        test("is empty is $condition") {
            assertEquals(condition, size == 0)
            if (condition)
                assertThrows<EmptyStackException> { peek() }
            else
                assertNotNull(peek())
        }
    }

    private fun TestContext<StringStack>.canPush() = {
        test("can push") {
            val initialSize = size
            val item = "*".repeat(initialSize + 1)
            push(item)
            assertEquals(item, peek())
            assertEquals(initialSize + 1, size)
        }
    }

    private fun TestContext<StringStack>.canPop() = {
        test("can pop") {
            val initialSize = size
            val top = peek()
            assertEquals(top, pop())
            assertEquals(initialSize - 1, size)
            if (size > 0)
                assertNotEquals(top, peek())
        }
    }

    @TestFactory fun `invoke functions to inject tests`() = context<StringStack> {

        fixture { StringStack() }

        context("an empty stack") {
            isEmpty(true)()
            canPush()
        }

        context("a stack with one item") {
            modifyFixture { push("one") }

            isEmpty(false)()
            canPush()()

            test("has the item on top") {
                assertEquals("one", pop())
            }

            canPop()
        }
    }

    @TestFactory fun `generate contexts to test with multiple values`() = context<StringStack> {

        (1..3).forEach { itemCount ->
            context("stack with $itemCount items") {

                fixture {
                    StringStack().apply {
                        (1..itemCount).forEach { add(it.toString()) }
                    }
                }

                isEmpty(false)()
                canPush()()
                canPop()()
            }
        }
    }
}