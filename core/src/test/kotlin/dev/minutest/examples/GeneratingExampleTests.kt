package dev.minutest.examples

import dev.minutest.ContextBuilder
import dev.minutest.experimental.willRun
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import java.util.*

// We can define extension functions that return tests for later injection

private typealias StringStack = Stack<String>

private fun ContextBuilder<StringStack>.isEmpty(isEmpty: Boolean) =
    test("is " + (if (isEmpty) "" else "not ") + "empty") {
        assertEquals(isEmpty, size == 0)
        if (isEmpty)
            assertThrows<EmptyStackException> { peek() }
        else
            assertNotNull(peek())
    }

private fun ContextBuilder<StringStack>.canPush() = test("can push") {
    val initialSize = size
    val item = "*".repeat(initialSize + 1)
    push(item)
    assertEquals(item, peek())
    assertEquals(initialSize + 1, size)
}

private fun ContextBuilder<StringStack>.canPop() = test("can pop") {
    val initialSize = size
    val top = peek()
    assertEquals(top, pop())
    assertEquals(initialSize - 1, size)
    if (size > 0)
        assertNotEquals(top, peek())
}

private fun ContextBuilder<StringStack>.cantPop() = test("cant pop") {
    assertThrows<EmptyStackException> { pop() }
}

class GeneratingExampleTests : JUnit5Minutests {

    fun tests() = rootContext<StringStack> {

        fixture { StringStack() }

        context("an empty stack") {
            // invoke the extension functions to create tests
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

        // Minutest will check that the following tests are run
        willRun(
            "▾ root",
            "  ▾ an empty stack",
            "    ✓ is empty",
            "    ✓ can push",
            "    ✓ cant pop",
            "  ▾ a stack with one item",
            "    ✓ is not empty",
            "    ✓ can push",
            "    ✓ can pop",
            "    ✓ has the item on top"
        )
    }
}

