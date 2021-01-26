package dev.minutest.examples

import dev.minutest.beforeEach
import dev.minutest.given
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.*

class StackExampleTests : JUnit5Minutests {

    fun tests() = rootContext<Stack<Any>> {

        given { Stack() }

        context("when new") {

            test("is empty") {
                assertTrue(it.isEmpty())
            }
        }

        context("after pushing an element") {

            beforeEach {
                it.push("an element")
            }

            test("it is no longer empty") {
                assertFalse(it.isEmpty())
            }
        }
    }
}
