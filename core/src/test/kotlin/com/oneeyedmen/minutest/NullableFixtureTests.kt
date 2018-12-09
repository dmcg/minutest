package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.JUnit5Minutests
import com.oneeyedmen.minutest.junit.context
import org.junit.jupiter.api.Assertions.assertNull


class NullableFixtureTests : JUnit5Minutests {

    override val tests = context<String?> {
        fixture { null }
        test("fixture is null") {
            assertNull(this)
        }
    }
}