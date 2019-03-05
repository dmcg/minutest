package dev.minutest

import dev.minutest.junit.JUnit5Minutests
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class VarianceTests : JUnit5Minutests {

    fun `can supply subtype of fixture type`() = rootContext<Number> {

        fixture { 42 }

        test("test") {
            // Not useless
            assertTrue(this is Int)

            @Suppress("USELESS_IS_CHECK")
            assertTrue(this is Number)

            assertEquals(42, this)
        }
    }
}