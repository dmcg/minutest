package dev.minutest

import dev.minutest.junit.JUnit5Minutests
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame


class FixtureSupplyingTests : JUnit5Minutests {

    fun `supply fixture at root`() = rootContext<String> {
        fixture { "banana" }
        context("parent had fixture") {
            test2("test") {
                assertEquals("banana", it)
            }
        }
    }

    fun `supply fixture in sub-context`() = rootContext<String> {
        context("parent had no fixture") {
            fixture { "banana" }
            test2("test") {
                assertEquals("banana", it)
            }
        }
    }

    fun `supply fixture in sub-sub-context`() = rootContext<String> {
        context("parent had no fixture") {
            context("parent had no fixture") {
                fixture { "banana" }
                test2("test") {
                    assertEquals("banana", it)
                }
            }
        }
    }

    fun `supply fixture in derivedContext`() = rootContext {
        derivedContext<String>("parent had no fixture") {
            fixture { "banana" }
            test2("test") {
                assertEquals("banana", it)
            }
        }
    }

    fun `need not specify Unit fixture`() = rootContext {
        test2("test") {
            assertSame(Unit, it)
        }
    }

    fun `can specify Unit root context`() = rootContext<Unit> {
        test2("test") {
            assertSame(Unit, it)
        }
    }

    fun `fixture can be nullable and null`() = rootContext<String?> {
        fixture { null }
        test2("fixture is null") {
            assertNull(it)
        }
    }

    fun `fixture can be nullable and not null`() = rootContext<String?> {
        fixture { "banana" }
        test2("fixture is not null") {
            assertNotNull(it)
        }
    }

    fun `derivedContext doesn't have to specify one if parent fixture will do`() = rootContext<String> {
        fixture { "banana" }
        derivedContext<String>("Hasn't actually changed type") {
            test2("test") {
                assertEquals("banana", it)
            }
        }
        derivedContext<CharSequence>("Has changed to a compatible type") {
            test2("test") {
                assertEquals("banana", it)
            }
        }
        derivedContext<String?>("Has changed to the nullable type") {
            test2("test") {
                assertEquals("banana", it)
            }
        }
    }

}