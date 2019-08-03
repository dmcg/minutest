package dev.minutest

import dev.minutest.junit.JUnit5Minutests
import kotlin.test.assertEquals


class AutofixtureTests : JUnit5Minutests {

    fun `fixture is not required in root`() = rootContext<Fixture> {
        context("parent had no fixture") {
            test("test") {
                assertEquals("default", this.value)
            }
        }
    }

    fun `supply fixture in sub-context`() = rootContext<Fixture> {
        context("parent had no fixture") {
            test("test") {
                assertEquals("default", this.value)
            }
        }
    }

    fun `supply fixture in sub-sub-context`() = rootContext<Fixture> {
        context("parent had no fixture") {
            context("parent had no fixture") {
                test("test") {
                    assertEquals("default", this.value)
                }
            }
        }
    }

    fun `supply fixture in derivedContext`() = rootContext {
        derivedContext<Fixture>("parent had no fixture") {
            test("test") {
                assertEquals("default", this.value)
            }
        }
    }

    fun `nullable fixture does not default to null`() = rootContext<Fixture?> {
        test("fixture is not null") {
            assertEquals("default", this?.value)
        }
    }

    fun `derivedContext doesn't create if parent fixture will do`() = rootContext<FixtureSubclass> {
        derivedContext<FixtureSubclass>("Hasn't actually changed type") {
            test("test") {
                assertEquals("subclass default", this.value)
            }
        }
        derivedContext<Fixture>("Has changed to a compatible type") {
            test("test") {
                assertEquals("subclass default", this.value)
            }
        }
        derivedContext<FixtureSubclass?>("Has changed to the nullable type") {
            test("test") {
                assertEquals("subclass default", this?.value)
            }
        }
    }

    open class Fixture() {
        var value: String = "default"
    }

    class FixtureSubclass : Fixture() {
        init {
            value = "subclass default"
        }
    }
}