package dev.minutest.examples.experimental

import dev.minutest.experimental.FOCUS
import dev.minutest.experimental.SKIP
import dev.minutest.experimental.minus
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test2
import kotlin.test.fail


class SkipAndFocusExampleTests : JUnit5Minutests {

    fun tests() = rootContext {

        // Apply the FOCUS annotation to a test
        FOCUS - test2("this test is focused, only other focused things will be run") {}

        context("not focused, so won't be run") {
            test2("would fail if the context was run") {
                fail("should not have run")
            }
        }

        context("contains a focused thing, so is run") {

            test2("isn't focused, so doesn't run") {
                fail("should not have run")
            }

            FOCUS - context("focused, so will be run") {

                test2("this runs") {}

                // apply the SKIP annotation to not run whatever
                SKIP - test2("skip overrides the focus") {
                    fail("should not have run")
                }

                SKIP - context("also applies to context") {
                    test2("will not be run") {
                        fail("should not have run")
                    }
                }
            }
        }
    }
}
