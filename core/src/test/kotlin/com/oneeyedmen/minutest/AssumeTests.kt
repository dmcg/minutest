package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.assertThrows
import org.opentest4j.TestAbortedException


class AssumeTests {

    @org.junit.jupiter.api.Test fun `assume works`() {

        val tests = junitTests<Unit> {
            test("try it") {
                assumeTrue("black".toLowerCase() == "white")
                fail("shouldn't get here")
            }
        }

        assertThrows<TestAbortedException> {
            executeTest(tests)
        }

    }
}