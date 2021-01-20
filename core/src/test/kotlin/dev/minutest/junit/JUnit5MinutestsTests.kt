package dev.minutest.junit

import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.*


// Just sanity checks in case we ever move away from this as the default runner
class JUnit5MinutestsTests : JUnit5Minutests {

    fun `plain fixture`() = rootContext<String> {
        fixture { "banana" }

        test("test") {
            assertEquals("banana", this)
        }
    }

    fun `generic fixture`() = rootContext<Stack<String>> {
        fixture { Stack() }

        test("test") {
            assertTrue(this.isEmpty())
        }
    }

    fun `nullable fixture`() = rootContext<String?> {
        fixture { "banana" }

        test("test") {
            assertEquals("banana", this ?: "kumquat")
        }
    }

    fun `nullable fixture that is null`() = rootContext<String?> {
        fixture { null }

        test("test") {
            assertEquals("kumquat", this ?: "kumquat")
        }
    }
}
