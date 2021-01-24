package dev.minutest.examples

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.*

class StackExampleTests : JUnit5Minutests {

    fun tests() = rootContext<Stack<Any>> {

        fixture { Stack() }

        context("when new") {

            test("is empty") {
                assertTrue(fixture.isEmpty())
            }
        }

        context("after pushing an element") {

            before {
                parentFixture.push("an element")
            }

            test("it is no longer empty") {
                assertFalse(fixture.isEmpty())
            }
        }
    }
}
