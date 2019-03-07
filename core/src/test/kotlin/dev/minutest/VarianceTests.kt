package dev.minutest

import dev.minutest.junit.JUnit5Minutests
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class VarianceTests : JUnit5Minutests {

    fun `can supply and return subtype of fixture type`() = rootContext<Number> {

        fixture { 42 }

        test_("test") {
            @Suppress("USELESS_IS_CHECK")
            assertTrue(this is Number)

            // Not useless
            assertTrue(this is Int)

            assertEquals(42, this)
            43
        }

        after {
            assertTrue(this is Int)
            assertEquals(43, this)
        }
    }

}