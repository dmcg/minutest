package dev.minutest

import dev.minutest.junit.JUnit5Minutests
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class VarianceTests : JUnit5Minutests {

    fun `can supply and return subtype of fixture type`() = rootContext<Number> {

        given { 42 }

        test_("test") {
            @Suppress("USELESS_IS_CHECK")
            assertTrue(this is Number)

            // Not useless
            assertTrue(it is Int)

            assertEquals(42, it)
            43
        }

        afterEach {
            assertTrue(it is Int)
            assertEquals(43, it)
        }
    }

}