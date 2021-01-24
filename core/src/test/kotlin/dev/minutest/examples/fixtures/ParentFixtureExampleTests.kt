package dev.minutest.examples.fixtures

import dev.minutest.beforeEach
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test2
import org.junit.jupiter.api.Assertions.assertEquals


class ParentFixtureExampleTests : JUnit5Minutests {

    data class Fixture(var fruit: String)

    fun tests() = rootContext<Fixture> {
        fixture {
            Fixture("banana")
        }

        test2("sees the context's fixture") {
            assertEquals("banana", it.fruit)
        }

        context("context inherits fixture") {
            test2("sees the parent context's fixture") {
                assertEquals("banana", it.fruit)
            }
        }

        context("context replaces fixture") {
            fixture {
                Fixture("kumquat")
            }
            test2("sees the replaced fixture") {
                assertEquals("kumquat", it.fruit)
            }
        }

        context("context modifies fixture") {
            beforeEach {
                it.fruit = "apple"
            }
            test2("sees the modified fixture") {
                assertEquals("apple", it.fruit)
            }
        }
    }
}
