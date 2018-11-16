package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.JupiterTests
import com.oneeyedmen.minutest.junit.context
import org.junit.jupiter.api.Assertions.assertEquals

class NoFixtureExampleTests : JupiterTests {

    override val tests = context<Unit> {

        context("addition") {
            test("positive + positive") {
                assertEquals(4, 3 + 1)
            }
            test("positive + negative") {
                assertEquals(2, 3 + -1)
            }
        }
        context("subtraction") {
            test("positive - positive") {
                assertEquals(2, 3 - 1)
            }
            test("positive - negative") {
                assertEquals(4, 3 - -1)
            }
        }
    }
}