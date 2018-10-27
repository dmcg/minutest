package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.fixturelessJunitTests
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.TestFactory


object AssumeTests {

    @TestFactory fun `assume works`() = fixturelessJunitTests {

        test("try it") {
            @Suppress("SimplifyBooleanWithConstants")
            assumeTrue("black" == "white")
            fail("shouldn't get here")
            // and, more to the point, the test runner shows a skipped test
        }
    }
}