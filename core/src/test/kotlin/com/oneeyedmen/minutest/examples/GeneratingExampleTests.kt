package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.TestContext
import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import java.util.EmptyStackException
import java.util.Stack

// We can define functions that return tests for later injection

private typealias StringStack = Stack<String>

private fun TestContext<StringStack>.isEmpty(isEmpty: Boolean) =
    test("is " + (if (isEmpty) "" else "not ") + "empty") {
        assertEquals(isEmpty, size == 0)
        if (isEmpty)
            assertThrows<EmptyStackException> { peek() }
        else
            assertNotNull(peek())
    }

private fun TestContext<StringStack>.canPush() = test("can push") {
    val initialSize = size
    val item = "*".repeat(initialSize + 1)
    push(item)
    assertEquals(item, peek())
    assertEquals(initialSize + 1, size)
}

private fun TestContext<StringStack>.canPop() = test("can pop") {
    val initialSize = size
    val top = peek()
    assertEquals(top, pop())
    assertEquals(initialSize - 1, size)
    if (size > 0)
        assertNotEquals(top, peek())
}

private fun TestContext<StringStack>.cantPop() = test("cant pop") {
    assertThrows<EmptyStackException> { pop() }
}

// In order to give multiple sets of tests, in this example we are using JUnit @TestFactory functions
class GeneratingExampleTests {

    // JUnit will run the tests from annotated functions
    @TestFactory fun `stack tests`() = junitTests<StringStack> {

        fixture { StringStack() }

        context("an empty stack") {
            // invoke the functions to create tests
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

    @TestFactory fun `multiple tests on multiple stacks`() = junitTests<StringStack> {

        fixture { StringStack() }

        // here we generate a context with 3 tests for each of 4 stacks
        (0..3).forEach { itemCount ->
            context("stack with $itemCount items") {

                modifyFixture {
                    (1..itemCount).forEach { add(it.toString()) }
                }

                isEmpty(itemCount == 0)
                canPush()
                canPop(itemCount > 0)
            }
        }
    }
}

private fun TestContext<StringStack>.canPop(canPop: Boolean) = if (canPop) canPop() else cantPop()
