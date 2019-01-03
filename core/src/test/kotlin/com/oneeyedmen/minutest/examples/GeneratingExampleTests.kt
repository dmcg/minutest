package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.TestContext
import com.oneeyedmen.minutest.experimental.checkedAgainst
import com.oneeyedmen.minutest.experimental.withTabsExpanded
import com.oneeyedmen.minutest.junit.JUnit5Minutests
import com.oneeyedmen.minutest.rootContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import java.util.*

// We can define extension functions that return tests for later injection

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

class GeneratingExampleTests : JUnit5Minutests {

    val summary = listOf(
        "▾ root",
        "    ▾ an empty stack",
        "        ✓ is empty",
        "        ✓ can push",
        "        ✓ cant pop",
        "    ▾ a stack with one item",
        "        ✓ is not empty",
        "        ✓ can push",
        "        ✓ can pop",
        "        ✓ has the item on top"
    )

    override val tests = rootContext<StringStack>(willRun(summary)) {

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
    }
}

private fun willRun(expectedLog: List<String>): (RuntimeNode<Unit>) -> RuntimeNode<Unit> =
    checkedAgainst { actualLog ->
        assertEquals(expectedLog, actualLog.withTabsExpanded(4))
    }
