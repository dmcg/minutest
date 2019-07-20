package dev.minutest.internal

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class ReflectingTests : JUnit5Minutests {

    fun tests() = rootContext {
        context("FixtureType") {
            test("captures class") {
                assertEquals(String::class, askType<String>().classifier)
            }
            test("captures nullability") {
                assertFalse(askType<String>().isMarkedNullable)
                assertTrue(askType<String?>().isMarkedNullable)
            }
            test("knows about subtypes") {
                assertTrue(askType<String>().isSubtypeOf(askType<Any>()))
                assertTrue(askType<String>().isSubtypeOf(askType<CharSequence>()))
                assertTrue(askType<String>().isSubtypeOf(askType<String>()))
                assertFalse(askType<String>().isSubtypeOf(askType<Int>()))
            }
            test("knows about nullability and subtypes") {
                assertTrue(askType<String>().isSubtypeOf(askType<String?>()))
                assertTrue(askType<String?>().isSubtypeOf(askType<String?>()))
                assertFalse(askType<String?>().isSubtypeOf(askType<String>()))
            }
        }
    }
}