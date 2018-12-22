package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.junit.JUnit5Minutests
import com.oneeyedmen.minutest.rootContext
import kotlin.test.fail


class SkipAndFocusExampleTests : JUnit5Minutests {

    // Skip and Focus (currently) require a transform to be installed to work
    override val tests = rootContext<Unit>(transform = skipAndFocus(), builder = {

        // Apply the FOCUS annotation to a test
        FOCUS - test("this test is focused, only other focused things will be run") {}

        context("not focused, so won't be run") {
            test("would fail if the context was run") {
                fail("should not have run")
            }
        }

        context("contains a focused thing, so is run") {

            test("isn't focused, so doesn't run") {
                fail("should not have run")
            }

            FOCUS - context("focused, so will be run") {

                test("this runs") {}

                // apply the SKIP annotation to not run whatever
                SKIP - test("skip overrides the focus") {
                    fail("should not have run")
                }
            }
        }
    })
}
