package dev.minutest

import dev.minutest.junit.JUnit5Minutests
import org.junit.jupiter.api.Assertions.assertEquals


class FixtureTests : JUnit5Minutests {

    data class Fixture(
        var fruit: String,
        val log: MutableList<String> = mutableListOf()
    )

    fun `with fixtures`() = rootContext<Fixture> {

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
            before { fruit = "green ${fruit}s" }

            test("sees the modified fixture") {
                assertEquals("green bananas", fruit)
            }

            context("sub-contexts see parent mods") {
                before { fruit = "we have no $fruit" }

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
}