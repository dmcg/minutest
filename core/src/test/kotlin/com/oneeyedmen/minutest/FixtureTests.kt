package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.toTestFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import java.io.FileNotFoundException


class FixtureTests {

    data class Fixture(
        var fruit: String,
        val log: MutableList<String> = mutableListOf()
    )

    @TestFactory fun `with fixtures`() = rootContext<Fixture> {

        fixture { Fixture("banana") }

        test("can mutate fixture without affecting following tests") {
            fruit = "kumquat"
            assertEquals("kumquat", fruit)
        }

        test("previous test did not affect me") {
            assertEquals("banana", fruit)
        }

        context("sub-context inheriting fixture") {
            test("has the fixture from its parent") {
                assertEquals("banana", fruit)
            }
        }

        context("sub-context overriding fixture") {
            fixture { Fixture("apple") }

            test("does not have the fixture from its parent") {
                assertEquals("apple", fruit)
            }
        }

        context("sub-context replacing fixture") {
            deriveFixture { Fixture("green $fruit") }

            test("sees the replaced fixture") {
                assertEquals("green banana", fruit)
            }
        }

        context("sub-context modifying fixture") {
            modifyFixture { fruit = "green ${fruit}s" }

            test("sees the modified fixture") {
                assertEquals("green bananas", fruit)
            }

            context("sub-contexts see parent mods") {
                modifyFixture { fruit = "we have no $fruit" }

                test("sees the modified fixture") {
                    assertEquals("we have no green bananas", fruit)
                }
            }
        }

        context("sanity check") {
            test("still not changed my context") {
                assertEquals("banana", fruit)
            }
        }
    }.toTestFactory()

    @TestFactory fun `no fixture`() = rootContext<Unit> {
        test("I need not specify Unit fixture") {
            assertNotNull("banana")
        }
    }.toTestFactory()

    @Test fun `throws IllegalStateException if no fixture specified when one is needed by a test`() {
        assertThrows<IllegalStateException> {
            rootContext<Fixture> {
                test("I report not having a fixture") {
                    assertEquals("banana", fruit)
                }
            }.toTestFactory()
        }
    }

    @Test fun `throws IllegalStateException if no fixture specified when one is needed by a fixture`() {
        assertThrows<IllegalStateException> {
            rootContext<Fixture> {
                modifyFixture {
                    this.fruit
                }
                test("I report not having a fixture") {
                    assertEquals("banana", fruit)
                }
            }.toTestFactory()
        }
    }

    @Test fun `throws IllegalStateException if fixture is specified twice in a context`() {
        assertThrows<IllegalStateException> {
            rootContext<Fixture> {
                fixture { Fixture("banana") }
                fixture { Fixture("banana") }
            }.toTestFactory()
        }
    }

    @Test fun `throws exception thrown from fixture`() {
        val tests = rootContext<Fixture> {
            fixture {
                throw FileNotFoundException()
            }

            test("won't be run") {
                assertEquals("banana", fruit)
            }
        }
        assertThrows<FileNotFoundException> {
            executeTests(tests)
        }
    }
}