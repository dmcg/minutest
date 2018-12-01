package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.TestFactory


class NullableFixtureTests {

    @TestFactory fun `nullable String`() = junitTests<String?> {
        fixture { null }
        test("fixture is null") {
            assertNull(this)
        }
    }
}