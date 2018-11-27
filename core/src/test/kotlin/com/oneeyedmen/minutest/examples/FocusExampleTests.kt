package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.experimental.FOCUS
import com.oneeyedmen.minutest.experimental.annotateWith
import com.oneeyedmen.minutest.experimental.minus
import com.oneeyedmen.minutest.junit.JupiterTests
import com.oneeyedmen.minutest.junit.context
import org.junit.Assert.fail


class FocusExampleTests : JupiterTests {

    override val tests = context<Unit>(FOCUS) {

        FOCUS - context("focused") {
            test("will run") {}
        }

        context("not focused") {
            test("won't run") {
                fail()
            }

            FOCUS - test("focused test inside not focused will run") {}

            FOCUS - context("focused inside not focused") {
                test("will run") {}
            }
        }

        context("another way of specifying focused") {
            annotateWith(FOCUS)
            test("will run") {}
        }
    }
}