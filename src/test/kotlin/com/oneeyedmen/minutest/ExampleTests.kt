package com.oneeyedmen.minutest

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import java.util.*


object ExampleTests {

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

    @TestFactory fun `raising the abstraction`() = context<Stack<String>> {

        // We can define functions that return tests for later injection

        fun TestContext<Stack<String>>.canPush() = {
            test("can push") {
                val initialSize = size
                val item = "*".repeat(initialSize + 1)
                push(item)
                assertEquals(item, peek())
                assertEquals(initialSize + 1, size)
            }
        }

        fun TestContext<Stack<String>>.canPop() = {
            test("can pop") {
                val initialSize = size
                val top = peek()
                assertEquals(top, pop())
                assertEquals(initialSize - 1, size)
                if (size > 0)
                    assertNotEquals(top, peek())
            }
        }

        fun TestContext<Stack<String>>.isEmpty(condition: Boolean): () -> MinuTest<Stack<String>> = {
            test("is empty is $condition") {
                assertEquals(condition, size == 0)
                if (condition)
                    assertThrows<EmptyStackException> { peek() }
                else
                    assertNotNull(peek())
            }
        }

        fixture { Stack() }

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

        // We can define tests and contexts on the fly
        context("stacks with more items") {

            (1..3).forEach { itemCount ->
                context("stack with $itemCount items") {

                    modifyFixture {
                        (1..itemCount).forEach { add(it.toString()) }
                    }

                    isEmpty(false)()
                    canPush()()
                    canPop()()
                }
            }
        }
    }
}