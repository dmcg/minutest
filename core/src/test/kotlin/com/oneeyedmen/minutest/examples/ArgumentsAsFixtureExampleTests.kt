package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.JupiterTests
import com.oneeyedmen.minutest.junit.context
import org.junit.jupiter.api.Assertions.assertEquals

class ArgumentsAsFixtureExampleTests : JupiterTests {

    data class Arguments(val l: Int, val r: Int)

    override val tests = context<Arguments> {

        context("positive positive") {
            fixture {
                Arguments(l = 3, r = 1)
            }
            test("addition") {
                assertEquals(4, l + r)
            }
            test("subtraction") {
                assertEquals(2, l - r)
            }
        }

        context("positive negative") {
            fixture {
                Arguments(l = 3, r = -1)
            }
            test("addition") {
                assertEquals(2, l + r)
            }
            test("subtraction") {
                assertEquals(4, l - r)
            }
        }
    }
}