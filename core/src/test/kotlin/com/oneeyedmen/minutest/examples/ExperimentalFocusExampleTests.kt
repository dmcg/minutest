package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.disabled
import com.oneeyedmen.minutest.experimental.just
import com.oneeyedmen.minutest.junit.JupiterTests
import com.oneeyedmen.minutest.junit.context
import org.junit.jupiter.api.fail

class ExperimentalFocusExampleTests : JupiterTests {

    override val tests = context<Unit> {

        disabled {
            test("my first test") {
                fail("Should not run")
            }
        }

        context("context") {

            just {
                context("will run") {
                    test("will run") {}
                }
            }

            test("won't run") {
                fail("Should not run")
            }
        }

        test("will run") {}
    }
}