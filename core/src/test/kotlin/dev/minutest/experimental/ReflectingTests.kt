package dev.minutest.experimental

import dev.minutest.internal.askType
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test2
import kotlin.test.assertEquals
import kotlin.test.assertNull


class ReflectingTests : JUnit5Minutests {

    fun tests() = rootContext {

        context("KType.creator") {
            context("works for") {
                test2("String") {
                    assertEquals("", askType<String>().creator()!!.invoke())
                }
                test2("String?") {
                    assertEquals("", askType<String?>().creator()!!.invoke())
                }
                test2("Unit") {
                    assertEquals(Unit, askType<Unit>().creator()!!.invoke())
                }
                test2("HashMap") {
                    assertEquals(HashMap<String, String>(), askType<HashMap<String, String>>().creator()!!.invoke())
                }
                test2("Any") {
                    assertEquals(Any::class, askType<Any>().creator()!!.invoke()::class)
                }
                test2("public object") {
                    assertEquals(PublicObject, askType<PublicObject>().creator()!!.invoke())
                }
            }
            context("is null for") {
                test2("private class") {
                    assertNull(askType<PrivateClass>().creator())
                }
                test2("public class with private ctor") {
                    assertNull(askType<PublicClassPrivateCtor>().creator())
                }
                test2("private object") {
                    assertNull(askType<PrivateObject>().creator())
                }
                test2("collection interfaces") {
                    assertNull(askType<List<*>>().creator())
                    assertNull(askType<Set<*>>().creator())
                }
                test2("primitives") {
                    assertNull(askType<Int>().creator())
                    assertNull(askType<Double>().creator())
                }
            }
        }
    }

}

private class PrivateClass

private class PublicClassPrivateCtor private constructor()

object PublicObject

private object PrivateObject