package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestFactory


class FixtureSupplyingTests {

    @TestFactory fun `supply fixture at top`() = junitTests("banana") {
        context("parent had no fixture") {
            test("test") {
                assertEquals("banana", this)
            }
        }
    }

    @TestFactory fun `supply fixture in derivedContext`() = junitTests<Unit> {
        derivedContext("parent had no fixture", "banana") {
            test("test") {
                assertEquals("banana", this)
            }
        }
    }

    @TestFactory fun `copes with no fixture if context has no operations`() = junitTests<Pair<Int, String>> {
        context("supplies the fixture") {
            fixture {
                42 to "the answer"
            }
            test("test") {
                assertEquals(42, this.first)
            }
        }
    }
}