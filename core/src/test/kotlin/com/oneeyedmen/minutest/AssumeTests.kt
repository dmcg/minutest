package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.JUnit5Minutests
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Assumptions.assumeTrue


class AssumeTests : JUnit5Minutests {

    override val tests = rootContext<Unit> {
        test("try it") {
            assumeTrue("black".toLowerCase() == "white")
            fail("shouldn't get here")
        }

        context("works in a fixture block") {
            modifyFixture {
                assumeTrue("black".toLowerCase() == "white")
            }
            test("try it") {
                fail("shouldn't get here")
            }

        }
    }
}