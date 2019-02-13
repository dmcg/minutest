package dev.minutest.internal

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class ReflectingTests : JUnit5Minutests {

    fun tests() = rootContext<Unit> {

        context("asKType") {
            test("captures class") {
                assertEquals(String::class, askType<String>().classifier)
            }
            test("captures nullability") {
                assertFalse(askType<String>().isMarkedNullable)
                assertTrue(askType<String?>().isMarkedNullable)
            }
        }
    }
}