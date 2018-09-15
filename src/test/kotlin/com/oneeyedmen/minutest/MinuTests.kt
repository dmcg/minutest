package com.oneeyedmen.minutest

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import kotlin.streams.asSequence


object MinuTests {

    data class Fixture(
        var thing: String,
        val log: MutableList<String> = mutableListOf<String>()
    )

    @TestFactory fun `with fixtures`() = context<Fixture> {

        fixture { Fixture("banana") }

        test("can mutate fixture without affecting following tests") {
            thing = "kumquat"
            assertEquals("kumquat", thing)
        }

        test("previous test did not affect me") {
            assertEquals("banana", thing)
        }

        context("sub-context inheriting fixture") {
            test("has the fixture from its parent") {
                assertEquals("banana", thing)
            }
        }

        context("sub-context overriding fixture") {
            fixture { Fixture("apple") }

            test("does not have the fixture from its parent") {
                assertEquals("apple", thing)
            }
        }

        context("sub-context replacing fixture") {
            replaceFixture { Fixture("green $thing") }

            test("sees the replaced fixture") {
                assertEquals("green banana", thing)
            }
        }

        context("sub-context modifying fixture") {
            modifyFixture { thing = "green ${thing}s" }

            test("sees the modified fixture") {
                assertEquals("green bananas", thing)
            }

            context("sub-contexts see parent mods") {
                modifyFixture { thing = "we have no $thing" }

                test("sees the modified fixture") {
                    assertEquals("we have no green bananas", thing)
                }
            }
        }

        context("sanity check") {
            test("still not changed my context") {
                assertEquals("banana", thing)
            }
        }
    }

    @TestFactory fun `dynamic generation`() = context<Fixture> {
        fixture { Fixture("banana") }

        context("same fixture for each") {
            (1..3).forEach { i ->
                test("test for $i") {}
            }
        }

        context("modify fixture for each test") {
            (1..3).forEach { i ->
                context("banana count $i") {
                    replaceFixture { Fixture("$i $thing") }
                    test("test for $i") {
                        assertEquals("$i banana", thing)
                    }
                }
            }
        }
    }

    @TestFactory fun `before and after`() = context<Fixture> {
        fixture { Fixture("banana") }

        before {
            assertTrue(log.isEmpty())
            log.add("before")
        }

        after {
            assertEquals(listOf("before", "during"), log)
            log.add("after")
        }

        test("before has been called") {
            assertEquals(listOf("before"), log)
            log.add("during")
        }

        context("also applies to contexts") {
            test("before is called") {
                assertEquals(listOf("before"), log)
                log.add("during")
            }
        }
    }

    @TestFactory fun `test transform`() = context<Fixture> {
        fixture { Fixture("banana") }

        modifyTests { test ->
            MinuTest(test.name) {
                // don't run
            }
        }

        test("transform can ignore test") {
            fail("Shouldn't get here")
        }
    }

    @TestFactory fun `no fixture`() = context<Unit> {
        test("I need not specify Unit fixture") {
            assertNotNull("banana")
        }
    }

    @Test fun `no fixture when one is needed`() {
        val tests: List<DynamicNode> = context<Fixture> {
            test("I report not having a fixture") {
                assertEquals("banana", thing)
            }
        }
        assertThrows<IllegalStateException> {
            ((tests.first() as DynamicContainer).children.asSequence().first() as DynamicTest).executable.execute()
        }
    }
}