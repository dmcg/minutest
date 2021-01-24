package dev.minutest.examples

import dev.minutest.ContextBuilder
import dev.minutest.experimental.willRun
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test2
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import java.util.*

// We can define extension functions that return tests for later injection

private typealias StringStack = Stack<String>

private fun ContextBuilder<StringStack>.isEmpty(isEmpty: Boolean) =
    test2("is " + (if (isEmpty) "" else "not ") + "empty") {
        assertEquals(it.isEmpty(), size == 0)
        if (isEmpty)
            assertThrows<EmptyStackException> { it.peek() }
        else
            assertNotNull(it.peek())
    }

private fun ContextBuilder<StringStack>.canPush() = test2("can push") {
    val initialSize = it.size
    val item = "*".repeat(initialSize + 1)
    it.push(item)
    assertEquals(item, it.peek())
    assertEquals(initialSize + 1, it.size)
}

private fun ContextBuilder<StringStack>.canPop() = test2("can pop") {
    val initialSize = it.size
    val top = it.peek()
    assertEquals(top, it.pop())
    assertEquals(initialSize - 1, size)
    if (it.size > 0)
        assertNotEquals(top, peek())
}

private fun ContextBuilder<StringStack>.cantPop() = test2("cant pop") {
    assertThrows<EmptyStackException> { it.pop() }
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
            before { push("one") }

            isEmpty(false)
            canPush()
            canPop()

            test2("has the item on top") {
                assertEquals("one", peek())
            }
        }

        // Minutest will check that the following tests are run
        willRun(
            "▾ tests",
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

