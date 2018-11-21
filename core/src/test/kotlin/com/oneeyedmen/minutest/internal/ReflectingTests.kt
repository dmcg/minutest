package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.junit.JupiterTests
import com.oneeyedmen.minutest.junit.context
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue


class ReflectingTests : JupiterTests {

    override val tests = context<Unit> {

        context("asKType") {
            test("captures class") {
                assertEquals(String::class, asKType<String>().classifier)
            }
            test("captures nullability") {
                assertFalse(asKType<String>().isMarkedNullable)
                assertTrue(asKType<String?>().isMarkedNullable)
            }
        }

        context("KType.creator") {
            context("works for") {
                test("String") {
                    assertEquals("", asKType<String>().creator()!!.invoke())
                }
                test("String?") {
                    assertEquals("", asKType<String?>().creator()!!.invoke())
                }
                test("Unit") {
                    assertEquals(Unit, asKType<Unit>().creator()!!.invoke())
                }
                test("HashMap") {
                    assertEquals(HashMap<String, String>(), asKType<HashMap<String, String>>().creator()!!.invoke())
                }
                test("Any") {
                    assertEquals(Any::class, asKType<Any>().creator()!!.invoke()::class)
                }
                test("public object") {
                    assertEquals(PublicObject, asKType<PublicObject>().creator()!!.invoke())
                }
            }
            context("is null for") {
                test("private class") {
                    assertNull(asKType<PrivateClass>().creator())
                }
                test("public class with private ctor") {
                    assertNull(asKType<PublicClassPrivateCtor>().creator())
                }
                test("private object") {
                    assertNull(asKType<PrivateObject>().creator())
                }
            }
        }
    }

}

private class PrivateClass

private class PublicClassPrivateCtor private constructor()

object PublicObject

private object PrivateObject