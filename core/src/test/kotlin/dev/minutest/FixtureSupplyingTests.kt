package dev.minutest

import dev.minutest.junit.JUnit5Minutests
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame


class FixtureSupplyingTests : JUnit5Minutests {

    fun `supply fixture at root`() = rootContext<String> {
        given { "banana" }
        context("parent had fixture") {
            test("test") {
                assertEquals("banana", it)
            }
        }
    }

    fun `supply fixture in sub-context`() = rootContext<String> {
        context("parent had no fixture") {
            given { "banana" }
            test("test") {
                assertEquals("banana", it)
            }
        }
    }

    fun `supply fixture in sub-sub-context`() = rootContext<String> {
        context("parent had no fixture") {
            context("parent had no fixture") {
                given { "banana" }
                test("test") {
                    assertEquals("banana", it)
                }
            }
        }
    }

    fun `supply fixture in derivedContext`() = rootContext {
        context_<String>("parent had no fixture") {
            given { "banana" }
            test("test") {
                assertEquals("banana", it)
            }
        }
    }

    fun `need not specify Unit fixture`() = rootContext {
        test("test") {
            assertSame(Unit, it)
        }
    }

    fun `can specify Unit root context`() = rootContext<Unit> {
        test("test") {
            assertSame(Unit, it)
        }
    }

    fun `fixture can be nullable and null`() = rootContext<String?> {
        given { null }
        test("fixture is null") {
            assertNull(it)
        }
    }

    fun `fixture can be nullable and not null`() = rootContext<String?> {
        given { "banana" }
        test("fixture is not null") {
            assertNotNull(it)
        }
    }

    fun `derivedContext doesn't have to specify one if parent fixture will do`() = rootContext<String> {
        given { "banana" }
        context_<String>("Hasn't actually changed type") {
            test("test") {
                assertEquals("banana", it)
            }
        }
        context_<CharSequence>("Has changed to a compatible type") {
            test("test") {
                assertEquals("banana", it)
            }
        }
        context_<String?>("Has changed to the nullable type") {
            test("test") {
                assertEquals("banana", it)
            }
        }
    }

}