@file:Suppress("unused", "UNUSED_PARAMETER")

package dev.minutest.internal

import dev.minutest.*
import dev.minutest.junit.JUnit5Minutests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.assertAll
import kotlin.reflect.KClass


class TestFindingTests : JUnit5Minutests {

    fun tests() = rootContext<Any> {

        context(ClassWithOnlyOneContextMethod::class) {
            test2("single root context from method") {
                val root = it.rootContextFromMethods(true) as Context<*, *>
                assertAll("root",
                    { assertEquals("test method", root.name) },
                    { assertEquals(listOf("test in tests"), root.children.map(Node<*>::name))}
                )
            }
        }

        context(ClassWithTwoContextMethods::class) {
            test2("single root context with child context from each method") {
                val root = it.rootContextFromMethods(true) as Context<*, *>
                assertAll("root",
                    { assertEquals("dev.minutest.internal.ClassWithTwoContextMethods", root.name) },
                    { assertEquals(listOf("tests", "testsToo"), root.children.map(Node<*>::name))}
                )
            }
        }

        context(ClassWithNoContextMethods::class) {
            test2("returns null") {
                assertNull(it.rootContextFromMethods(true))
            }
        }

        context(ClassWithNoPublicContextMethods::class) {
            test2("returns null") {
                assertNull(it.rootContextFromMethods(true))
            }
        }

        context(ClassWithNoZeroArgContextMethod::class) {
            test2("returns null") {
                assertNull(it.rootContextFromMethods(true))
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
        test2("test in tests") {}
    }
}

class ClassWithTwoContextMethods {
    fun tests() = rootContext<String> {
        fixture { "banana" }
        test2("test in tests") {}
    }
    fun testsToo() = rootContext {
        test2("test in testsToo") {}
    }
}

class ClassWithNoContextMethods

class ClassWithNoPublicContextMethods {
    private fun tests() = rootContext {}
}

class ClassWithNoZeroArgContextMethod {
    fun tests(x: Int) = rootContext {}
}