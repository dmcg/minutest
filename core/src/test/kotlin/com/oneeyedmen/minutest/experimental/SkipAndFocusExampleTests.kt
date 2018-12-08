package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.junit.JupiterTests
import com.oneeyedmen.minutest.junit.context
import kotlin.test.fail


class SkipAndFocusExampleTests : JupiterTests {

    override val tests = context<Unit>(skipAndFocus) {

        FOCUS - test("this test is focused, only focused things will be run") {}

        context("not focused, so won't be run") {
            test("would fail") {
                fail("should not have run")
            }
        }

        context("contains a focused thing") {

            test("would fail, but isn't focused") {
                fail("should not have run")
            }

            FOCUS - context("focused, so will be run") {
                test("this runs") {}
                SKIP - test("skip overrides the focus") {
                    fail("should not have run")
                }
            }
        }
    }

}
