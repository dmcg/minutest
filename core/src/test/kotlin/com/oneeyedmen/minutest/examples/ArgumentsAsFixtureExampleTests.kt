package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.JupiterTests
import com.oneeyedmen.minutest.junit.context
import org.junit.jupiter.api.Assertions.assertEquals

class ArgumentsAsFixtureExampleTests : JupiterTests {

    data class Arguments(val a: Int, val b: Int)

    override val tests = context<Arguments> {

        context("positive positive") {
            fixture {
                Arguments(3, 1)
            }
            test("addition") {
                assertEquals(4, a + b)
            }
            test("subtraction") {
                assertEquals(2, a - b)
            }
        }

        context("positive negative") {
            fixture {
                Arguments(3, -1)
            }
            test("addition") {
                assertEquals(2, a + b)
            }
            test("subtraction") {
                assertEquals(4, a - b)
            }
        }
    }
}