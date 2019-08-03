package dev.minutest.experimental

import dev.minutest.internal.FixtureType
import dev.minutest.internal.askType
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import kotlin.test.assertEquals
import kotlin.test.assertNull


class ReflectingTests : JUnit5Minutests {

    fun askTypeTests() = rootContext {
        test("String") {
            assertEquals(FixtureType(String::class, false), askType<String>())
        }
        test("String?") {
            assertEquals(FixtureType(String::class, true), askType<String?>())
        }
        test("Unit") {
            assertEquals(FixtureType(Unit::class, false), askType<Unit>())
        }
        test("Any") {
            assertEquals(FixtureType(Any::class, false), askType<Any>())
        }
        test("public object") {
            assertEquals(FixtureType(PublicObject::class, false), askType<PublicObject>())
        }
        test("private object") {
            assertEquals(FixtureType(PrivateObject::class, false), askType<PrivateObject>())
        }
    }

    fun `FixtureType creator`() = rootContext {
        context("works for") {
            test("String") {
                assertEquals("", askType<String>().creator()!!.invoke())
            }
            test("String?") {
                assertEquals("", askType<String?>().creator()!!.invoke())
            }
            test("Unit") {
                assertEquals(Unit, askType<Unit>().creator()!!.invoke())
            }
            test("HashMap") {
                assertEquals(HashMap<String, String>(), askType<HashMap<String, String>>().creator()!!.invoke())
            }
            test("Any") {
                assertEquals(Any::class, askType<Any>().creator()!!.invoke()::class)
            }
            test("public object") {
                assertEquals(PublicObject, askType<PublicObject>().creator()!!.invoke())
            }
        }
        context("is null for") {
            test("private class") {
                assertNull(askType<PrivateClass>().creator())
            }
            test("public class with private ctor") {
                assertNull(askType<PublicClassPrivateCtor>().creator())
            }
            test("private object") {
                assertNull(askType<PrivateObject>().creator())
            }
            test("collection interfaces") {
                assertNull(askType<List<*>>().creator())
                assertNull(askType<Set<*>>().creator())
            }
            test("primitives") {
                assertNull(askType<Int>().creator())
                assertNull(askType<Double>().creator())
            }
        }
    }
}

private class PrivateClass

private class PublicClassPrivateCtor private constructor()

object PublicObject

private object PrivateObject