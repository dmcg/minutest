package dev.minutest.examples

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

@DisplayName("A stack")
class TestingAStackDemo {

    var stack: Stack<Any> = Stack()

    @Nested
    @DisplayName("when new")
    inner class WhenNew {

        @Test
        fun `is empty`() {
            assertTrue(stack.isEmpty())
        }
    }

    @Nested
    @DisplayName("after pushing an element")
    inner class AfterPushing {

        var anElement = "an element"

        @BeforeEach
        fun pushAnElement() {
            stack.push(anElement)
        }

        @Test
        fun `it is no longer empty`() {
            assertFalse(stack.isEmpty())
        }
    }
}