package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestFactory


object FixtureSupplyingTests {

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

    @TestFactory fun `call no arg ctor when no fixture supplied`() = junitTests<String> {
        context("will get default fixture") {
            test("test") {
                assertEquals("", this)
            }
        }
        context("overrides default fixture") {
            fixture {
                assertEquals("", this)
                "banana"
            }
            test("test") {
                assertEquals("banana", this)
            }
        }
    }
}