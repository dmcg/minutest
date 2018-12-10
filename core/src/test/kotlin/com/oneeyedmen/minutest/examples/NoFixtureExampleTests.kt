package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.JUnit5Minutests
import com.oneeyedmen.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertEquals

class NoFixtureExampleTests : JUnit5Minutests {

    override val tests = rootContext<Unit> {

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