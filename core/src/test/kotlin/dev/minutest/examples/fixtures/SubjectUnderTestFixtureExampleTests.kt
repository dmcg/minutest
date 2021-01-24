package dev.minutest.examples.fixtures

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test2
import org.junit.jupiter.api.Assertions.*

class SubjectUnderTestFixtureExampleTests : JUnit5Minutests {

    fun tests() = rootContext<List<String>> {

        context("empty") {
            fixture {
                emptyList()
            }
            test2("is empty") {
                assertTrue(fixture.isEmpty())
            }
            test2("no head") {
                assertNull(fixture.firstOrNull())
            }
        }

        // Note that the context name and the fixture state agree
        context("not empty") {
            fixture {
                listOf("item")
            }
            test2("is not empty") {
                assertFalse(fixture.isEmpty())
            }
            test2("has head") {
                assertEquals("item", fixture.firstOrNull())
            }
        }
    }
}