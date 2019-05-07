package dev.minutest

import dev.minutest.junit.JUnit5Minutests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame


class FixtureSupplyingTests : JUnit5Minutests {

    fun `supply fixture at top`() = rootContext<String> {
        fixture { "banana" }
        context("parent had fixture") {
            test("test") {
                assertEquals("banana", this)
            }
        }
    }

    fun `supply fixture in sub-context`() = rootContext<String> {
        derivedContext<String>("parent had no fixture") {
            fixture { "banana" }
            test("test") {
                assertEquals("banana", this)
            }
        }
    }

    fun `supply fixture in derivedContext`() = rootContext {
        derivedContext<String>("parent had no fixture") {
            fixture { "banana" }
            test("test") {
                assertEquals("banana", this)
            }
        }
    }

    fun `need not specify Unit fixture`() = rootContext {
        test("test") {
            assertSame(Unit, fixture)
        }
    }


}