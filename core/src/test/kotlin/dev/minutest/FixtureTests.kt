package dev.minutest

import dev.minutest.junit.JUnit5Minutests
import org.junit.jupiter.api.Assertions.assertEquals


class FixtureTests : JUnit5Minutests {

    data class Fixture(
        var fruit: String,
        val log: MutableList<String> = mutableListOf()
    )

    fun `with fixtures`() = rootContext<Fixture> {

        given { Fixture("banana") }

        test2("can mutate fixture without affecting following tests") {
            fruit = "kumquat"
            assertEquals("kumquat", fruit)
        }

        test2("previous test did not affect me") {
            assertEquals("banana", fruit)
        }

        context("sub-context inheriting fixture") {
            test2("has the fixture from its parent") {
                assertEquals("banana", fruit)
            }
        }

        context("sub-context overriding fixture") {
            given { Fixture("apple") }

            test2("does not have the fixture from its parent") {
                assertEquals("apple", fruit)
            }
        }

        context("sub-context replacing fixture") {
            given_ { parentFixture -> Fixture("green ${parentFixture.fruit}") }

            test2("sees the replaced fixture") {
                assertEquals("green banana", fruit)
            }
        }

        context("sub-context modifying fixture") {
            beforeEach { fruit = "green ${fruit}s" }

            test2("sees the modified fixture") {
                assertEquals("green bananas", fruit)
            }

            context("sub-contexts see parent mods") {
                beforeEach { fruit = "we have no $fruit" }

                test2("sees the modified fixture") {
                    assertEquals("we have no green bananas", fruit)
                }
            }
        }

        context("sanity check") {
            test2("still not changed my context") {
                assertEquals("banana", fruit)
            }
        }
    }
}