package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.fixturelessJunitTests
import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import java.io.FileNotFoundException
import kotlin.streams.asSequence


object FixtureTests {

    data class Fixture(
        var fruit: String,
        val log: MutableList<String> = mutableListOf()
    )

    @TestFactory fun `with fixtures`() = junitTests<Fixture> {

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
            fixture { Fixture("green $fruit") }

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
    }

    @TestFactory fun `no fixture`() = fixturelessJunitTests() {
        test("I need not specify Unit fixture") {
            assertNotNull("banana")
        }
    }

    @Test fun `throws IllegalStateException if no fixture specified when one is needed by a test`() {
        val tests = junitTests<Fixture> {
            test("I report not having a fixture") {
                assertEquals("banana", fruit)
            }
        }.asSequence()
        assertThrows<IllegalStateException> {
            ((tests.first() as DynamicTest)).executable.execute()
        }
    }

    @Test fun `throws IllegalStateException if no fixture specified when one is needed by a fixture`() {
        val tests = junitTests<Fixture> {
            modifyFixture {
                this.fruit
            }
            test("I report not having a fixture") {
                assertEquals("banana", fruit)
            }
        }.asSequence()
        assertThrows<IllegalStateException> {
            ((tests.first() as DynamicTest)).executable.execute()
        }
    }

    @Test fun `throws IllegalStateException if fixture is specified twice in a context`() {
        assertThrows<IllegalStateException> {
            junitTests<Fixture> {
                fixture { Fixture("banana") }
                fixture { Fixture("banana") }
            }
        }
    }

    @Test fun `throws exception thrown from fixture`() {
        val tests = junitTests<Fixture> {
            fixture {
                throw FileNotFoundException()
            }

            test("won't be run") {
                assertEquals("banana", fruit)
            }
        }.asSequence()
        assertThrows<FileNotFoundException> {
            ((tests.first() as DynamicTest)).executable.execute()
        }
    }
}