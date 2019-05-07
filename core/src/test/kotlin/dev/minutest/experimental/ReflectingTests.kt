package dev.minutest.experimental

import dev.minutest.internal.askType
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import kotlin.test.assertEquals
import kotlin.test.assertNull


class ReflectingTests : JUnit5Minutests {

    fun tests() = rootContext {

        context("KType.creator") {
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
            }
        }
    }

}

private class PrivateClass

private class PublicClassPrivateCtor private constructor()

object PublicObject

private object PrivateObject