package dev.minutest.examples

import dev.minutest.beforeEach
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test2
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.*

class StackExampleTests : JUnit5Minutests {

    fun tests() = rootContext<Stack<Any>> {

        fixture { Stack() }

        context("when new") {

            test2("is empty") {
                assertTrue(it.isEmpty())
            }
        }

        context("after pushing an element") {

            beforeEach {
                it.push("an element")
            }

            test2("it is no longer empty") {
                assertFalse(it.isEmpty())
            }
        }
    }
}
