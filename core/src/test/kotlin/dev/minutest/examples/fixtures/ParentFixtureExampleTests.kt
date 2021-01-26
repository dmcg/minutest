package dev.minutest.examples.fixtures

import dev.minutest.beforeEach
import dev.minutest.given
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test
import org.junit.jupiter.api.Assertions.assertEquals


class ParentFixtureExampleTests : JUnit5Minutests {

    data class Fixture(var fruit: String)

    fun tests() = rootContext<Fixture> {
        given {
            Fixture("banana")
        }

        test("sees the context's fixture") {
            assertEquals("banana", it.fruit)
        }

        context("context inherits fixture") {
            test("sees the parent context's fixture") {
                assertEquals("banana", it.fruit)
            }
        }

        context("context replaces fixture") {
            given {
                Fixture("kumquat")
            }
            test("sees the replaced fixture") {
                assertEquals("kumquat", it.fruit)
            }
        }

        context("context modifies fixture") {
            beforeEach {
                it.fruit = "apple"
            }
            test("sees the modified fixture") {
                assertEquals("apple", it.fruit)
            }
        }
    }
}
