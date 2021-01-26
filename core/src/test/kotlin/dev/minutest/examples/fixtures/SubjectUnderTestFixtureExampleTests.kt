package dev.minutest.examples.fixtures

import dev.minutest.given
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test
import org.junit.jupiter.api.Assertions.*

class SubjectUnderTestFixtureExampleTests : JUnit5Minutests {

    fun tests() = rootContext<List<String>> {

        context("empty") {
            given {
                emptyList()
            }
            test("is empty") {
                assertTrue(it.isEmpty())
            }
            test("no head") {
                assertNull(it.firstOrNull())
            }
        }

        // Note that the context name and the fixture state agree
        context("not empty") {
            given {
                listOf("item")
            }
            test("is not empty") {
                assertFalse(it.isEmpty())
            }
            test("has head") {
                assertEquals("item", it.firstOrNull())
            }
        }
    }
}