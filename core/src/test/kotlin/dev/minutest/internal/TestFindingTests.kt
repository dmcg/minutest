@file:Suppress("unused", "UNUSED_PARAMETER")

package dev.minutest.internal

import dev.minutest.Context
import dev.minutest.ContextBuilder
import dev.minutest.Node
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import kotlin.reflect.KClass


class TestFindingTests : JUnit5Minutests {

    fun tests() = rootContext<Any> {

        context(ClassWithOnlyOneContextMethod::class) {
            test("single root context from method") {
                val root = fixture.rootContextFromMethods() as Context<*, *>
                assertAll("root",
                    { assertEquals("test method", root.name) },
                    { assertEquals(listOf("test in tests"), root.children.map(Node<*>::name))}
                )
            }
        }

        context(ClassWithTwoContextMethods::class) {
            test("single root context with child context from each method") {
                val root = fixture.rootContextFromMethods() as Context<*, *>
                assertAll("root",
                    { assertEquals("dev.minutest.internal.ClassWithTwoContextMethods", root.name) },
                    { assertEquals(listOf("tests", "testsToo"), root.children.map(Node<*>::name))}
                )
            }
        }

        context(ClassWithNoContextMethods::class) {
            test("raises error") {
                assertThrows<RuntimeException> {
                    fixture.rootContextFromMethods()
                }
            }
        }

        context(ClassWithNoPublicContextMethods::class) {
            test("raises error") {
                assertThrows<RuntimeException> {
                    fixture.rootContextFromMethods()
                }
            }
        }

        context(ClassWithNoZeroArgContextMethod::class) {
            test("raises error") {
                assertThrows<RuntimeException> {
                    fixture.rootContextFromMethods()
                }
            }
        }
    }

    private fun ContextBuilder<Any>.context(type: KClass<*>, tests: ContextBuilder<Any>.() -> Unit) = context(type.simpleName ?: "UNKNOWN") {
        fixture { type.java.newInstance() }
        tests()
    }
}

class ClassWithOnlyOneContextMethod {
    fun `test method`() = rootContext<String> {
        fixture { "banana" }
        test("test in tests") {}
    }
}

class ClassWithTwoContextMethods {
    fun tests() = rootContext<String> {
        fixture { "banana" }
        test("test in tests") {}
    }
    fun testsToo() = rootContext {
        test("test in testsToo") {}
    }
}

class ClassWithNoContextMethods

class ClassWithNoPublicContextMethods {
    private fun tests() = rootContext {}
}

class ClassWithNoZeroArgContextMethod {
    fun tests(x: Int) = rootContext {}
}