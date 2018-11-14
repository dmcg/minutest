package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.JupiterTests
import com.oneeyedmen.minutest.junit.context
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.*

class FixtureExampleTests : JupiterTests {

    // We have multiple items of test state, so make a separate fixture class
    class Fixture {
        // these would be the fields of your JUnit test
        val stack1 = Stack<Int>()
        val stack2 = Stack<Int>()
    }

    // now our context type is Fixture
    override val tests = context<Fixture> {

        // create it in a fixture block
        fixture { Fixture() }

        // and access it in tests
        test("add all single item") {
            stack2.add(1)
            stack1.addAll(stack2)
            assertEquals(listOf(1), stack1.toList())
        }

        test("add all multiple items") {
            assertTrue(stack1.isEmpty() && stack2.isEmpty(), "fixture is clean each test")
            stack2.addAll(listOf(1, 2))
            stack1.addAll(stack2)
            assertEquals(listOf(1, 2), stack1.toList())
        }
    }
}