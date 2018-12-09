package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.context
import com.oneeyedmen.minutest.junit.toTestFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestFactory


class FixtureSupplyingTests {

    @TestFactory fun `supply fixture at top`() = context<String> {
        fixture { "banana" }
        context("parent had fixture") {
            test("test") {
                assertEquals("banana", this)
            }
        }
    }.toTestFactory()

    @TestFactory fun `supply fixture in derivedContext`() = context<Unit> {
        derivedContext<String>("parent had no fixture") {
            fixture { "banana" }
            test("test") {
                assertEquals("banana", this)
            }
        }
    }.toTestFactory()

    @TestFactory fun `copes with no fixture if context has no operations`() = context<Pair<Int, String>> {
        context("supplies the fixture") {
            fixture {
                42 to "the answer"
            }
            test("test") {
                assertEquals(42, this.first)
            }
        }
    }.toTestFactory()
}