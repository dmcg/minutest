package dev.minutest.junit

import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.*


class JUnit5TestsWithFunction : JUnit5Minutests {

    fun myTests() = rootContext<String> {
        fixture { "banana" }

        test("test") {
            assertEquals("banana", this)
        }
    }
}

class JUnit5TestsWithPlainFixture : JUnit5Minutests {

    fun tests() = rootContext<String> {
        fixture { "banana" }

        test("test") {
            assertEquals("banana", this)
        }
    }
}

class JUnit5TestsWithGenericFixture : JUnit5Minutests {

    fun tests() = rootContext<Stack<String>> {
        fixture { Stack() }

        test("test") {
            assertTrue(this.isEmpty())
        }
    }
}

class JUnit5TestsWithNullableFixture : JUnit5Minutests {

    fun tests() = rootContext<String?> {
        fixture { "banana" }

        test("test") {
            val copy: String = this ?: "kumquat"
            assertEquals("banana", copy)
        }
    }
}
