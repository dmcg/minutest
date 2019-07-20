package dev.minutest

import dev.minutest.junit.JUnit5Minutests
import org.junit.jupiter.api.Assertions.*


class FixtureSupplyingTests : JUnit5Minutests {

    fun `supply fixture at top`() = rootContext<String> {
        fixture { "banana" }
        context("parent had fixture") {
            test("test") {
                assertEquals("banana", this)
            }
        }
    }

    fun `supply fixture in sub-context`() = rootContext<String> {
        context("parent had no fixture") {
            fixture { "banana" }
            test("test") {
                assertEquals("banana", this)
            }
        }
    }

    fun `supply fixture in sub-sub-context`() = rootContext<String> {
        context("parent had no fixture") {
            context("parent had no fixture") {
                fixture { "banana" }
                test("test") {
                    assertEquals("banana", this)
                }
            }
        }
    }

    fun `supply fixture in derivedContext`() = rootContext {
        derivedContext<String>("parent had no fixture") {
            fixture { "banana" }
            test("test") {
                assertEquals("banana", this)
            }
        }
    }

    fun `need not specify Unit fixture`() = rootContext {
        test("test") {
            assertSame(Unit, fixture)
        }
    }

    fun `can specify Unit root context`() = rootContext<Unit> {
        test("test") {
            assertSame(Unit, fixture)
        }
    }

    fun `fixture can be nullable and null`() = rootContext<String?> {
        fixture { null }
        test("fixture is null") {
            assertNull(this)
        }
    }

    fun `fixture can be nullable and not null`() = rootContext<String?> {
        fixture { "banana" }
        test("fixture is not null") {
            assertNotNull(this)
        }
    }

    fun `derivedContext doesn't have to specify one if parent fixture will do`() = rootContext<String> {
        fixture { "banana" }
        derivedContext<String>("Hasn't actually changed type") {
            test("test") {
                assertEquals("banana", this)
            }
        }
        derivedContext<CharSequence>("Has changed to a compatible type") {
            test("test") {
                assertEquals("banana", this)
            }
        }
        derivedContext<String?>("Has changed to the nullable type") {
            test("test") {
                assertEquals("banana", this)
            }
        }
    }

}