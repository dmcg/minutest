package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.experimental.SKIP
import com.oneeyedmen.minutest.experimental.minus
import com.oneeyedmen.minutest.junit.JupiterTests
import com.oneeyedmen.minutest.junit.context
import org.junit.Assert.fail


class SkipExampleTests : JupiterTests {

    override val tests = context<Unit>(SKIP) {
        test("will run") {}

        SKIP - context("skipped") {
            test("won't run") {
                fail()
            }
        }
    }
}