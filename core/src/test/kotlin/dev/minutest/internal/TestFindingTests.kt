package dev.minutest.internal

import dev.minutest.Context
import dev.minutest.Node
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.junit.rootContextFromMethods
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows


class TestFindingTests : JUnit5Minutests {

    fun tests() = rootContext<Any> {

        context("class with only one context method") {
            fixture { ClassWithOnlyOneContextMethod() }

            test("single root context from method") {
                val root = fixture.rootContextFromMethods() as Context<*, *>
                assertAll("root",
                    { assertEquals("root", root.name) },
                    { assertEquals(listOf("test in tests"), root.children.map(Node<*>::name))}
                )
            }
        }

        context("class with two context methods") {
            fixture { ClassWithTwoContextMethods() }

            test("single root context with child context from each method") {
                val root = fixture.rootContextFromMethods() as Context<*, *>
                assertAll("root",
                    { assertEquals("root", root.name) },
                    { assertEquals(listOf("tests", "testsToo"), root.children.map(Node<*>::name))}
                )
            }
        }

        context("class with no context methods") {
            fixture { ClassWithNoContextMethods() }

            test("raises error") {
                assertThrows<RuntimeException> {
                    fixture.rootContextFromMethods()
                }
            }
        }

        context("class with no public context methods") {
            fixture { ClassWithNoPublicContextMethods() }

            test("raises error") {
                assertThrows<RuntimeException> {
                    fixture.rootContextFromMethods()
                }
            }
        }
    }
}

class ClassWithOnlyOneContextMethod {
    fun tests() = rootContext<String> {
        fixture { "banana" }
        test("test in tests") {}
    }
}

class ClassWithTwoContextMethods {
    fun tests() = rootContext<String> {
        fixture { "banana" }
        test("test in tests") {}
    }
    fun testsToo() = rootContext<Unit> {
        test("test in testsToo") {}
    }
}

class ClassWithNoContextMethods

class ClassWithNoPublicContextMethods {
    private fun tests() = rootContext<Unit> {}
}