package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.experimental.*
import org.junit.Assert.fail
import org.junit.jupiter.api.TestFactory


class PostBuildTransformsExampleTests {

    @TestFactory fun skipRoot() = transformedJunitTests<Unit>(SKIP) {
        annotateWith(SKIP)

        test("won't run") {
            fail()
        }
    }

    @TestFactory fun skipContext() = transformedJunitTests<Unit>(SKIP) {

        test("will run") {}

        SKIP - context("skipped") {
            test("won't run") {
                fail()
            }
        }

    }

    @TestFactory fun focusContext() = transformedJunitTests<Unit>(FOCUS) {

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