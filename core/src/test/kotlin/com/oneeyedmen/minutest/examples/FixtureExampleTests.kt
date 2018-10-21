package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.JupiterTests
import com.oneeyedmen.minutest.junit.context
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.*

object FixtureExampleTests : JupiterTests {

    // We have multiple state, so make a separate fixture class
    class Fixture {
        // these would be the fields of your JUnit test
        val stack1 = Stack<String>()
        val stack2 = Stack<String>()
    }

    // now our context type is Fixture
    override val tests = context<Fixture> {
        // Again the fixture is created once for each test
        fixture { Fixture() }

        // and access it in tests
        test("swap top") {
            stack1.push("on one")
            stack2.push("on two")
            stack1.swapTop(stack2)
            assertEquals("on two", stack1.peek())
            assertEquals("on one", stack2.peek())
        }
    }
}

private fun <E> Stack<E>.swapTop(otherStack: Stack<E>) {
    val myTop = pop()
    push(otherStack.pop())
    otherStack.push(myTop)
}