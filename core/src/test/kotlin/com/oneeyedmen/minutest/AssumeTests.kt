package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.JUnit5Minutests
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Assumptions.assumeTrue


class AssumeTests : JUnit5Minutests {

    override val tests = rootContext<Unit> {

        test("assume in a test aborts it") {
            assumeTrue("black".toLowerCase() == "white")
            fail("shouldn't get here")
        }

        context("a context with assume in a fixture block") {
            modifyFixture {
                assumeTrue("black".toLowerCase() == "white")
            }
            test("should not be run") {
                fail("shouldn't get here")
            }

        }
    }
}