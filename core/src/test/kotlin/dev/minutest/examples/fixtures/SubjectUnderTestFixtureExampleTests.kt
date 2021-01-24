package dev.minutest.examples.fixtures

import dev.minutest.given
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test2
import org.junit.jupiter.api.Assertions.*

class SubjectUnderTestFixtureExampleTests : JUnit5Minutests {

    fun tests() = rootContext<List<String>> {

        context("empty") {
            given {
                emptyList()
            }
            test2("is empty") {
                assertTrue(it.isEmpty())
            }
            test2("no head") {
                assertNull(it.firstOrNull())
            }
        }

        // Note that the context name and the fixture state agree
        context("not empty") {
            given {
                listOf("item")
            }
            test2("is not empty") {
                assertFalse(it.isEmpty())
            }
            test2("has head") {
                assertEquals("item", it.firstOrNull())
            }
        }
    }
}