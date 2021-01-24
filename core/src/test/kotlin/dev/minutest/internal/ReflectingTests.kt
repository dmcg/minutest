package dev.minutest.internal

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test2
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class ReflectingTests : JUnit5Minutests {

    fun tests() = rootContext {
        context("FixtureType") {
            test2("captures class") {
                assertEquals(String::class, askType<String>().classifier)
            }
            test2("captures nullability") {
                assertFalse(askType<String>().isNullable)
                assertTrue(askType<String?>().isNullable)
            }
            test2("knows about subtypes") {
                assertTrue(askType<String>().isSubtypeOf(askType<Any>()))
                assertTrue(askType<String>().isSubtypeOf(askType<CharSequence>()))
                assertTrue(askType<String>().isSubtypeOf(askType<String>()))
                assertFalse(askType<String>().isSubtypeOf(askType<Int>()))
            }
            test2("knows about nullability and subtypes") {
                assertTrue(askType<String>().isSubtypeOf(askType<String?>()))
                assertTrue(askType<String?>().isSubtypeOf(askType<String?>()))
                assertFalse(askType<String?>().isSubtypeOf(askType<String>()))
            }
        }
    }
}