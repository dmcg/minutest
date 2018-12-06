package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.JupiterTests
import com.oneeyedmen.minutest.junit.context
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Assumptions.assumeTrue


class AssumeTests : JupiterTests {

    override val tests = context<Unit> {
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