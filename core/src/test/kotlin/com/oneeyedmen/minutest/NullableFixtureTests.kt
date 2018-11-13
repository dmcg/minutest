package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.TestFactory


class NullableFixtureTests {

    @TestFactory fun `nullable String`() = junitTests<String?>(null) {
        test("fixture is null") {
            assertNull(this)
        }
    }

    @TestFactory fun `nullable String with late fixture`() = junitTests<String?> {
        fixture { null }

        test("fixture is null") {
            assertNull(this)
        }
    }
}