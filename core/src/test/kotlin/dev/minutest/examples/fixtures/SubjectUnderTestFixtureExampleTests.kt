package dev.minutest.examples.fixtures

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.*

class SubjectUnderTestFixtureExampleTests : JUnit5Minutests {

    fun tests() = rootContext<List<String>> {

        context("empty") {
            fixture {
                emptyList()
            }
            test("is empty") {
                assertTrue(fixture.isEmpty())
            }
            test("no head") {
                assertNull(fixture.firstOrNull())
            }
        }

        // Note that the context name and the fixture state agree
        context("not empty") {
            fixture {
                listOf("item")
            }
            test("is not empty") {
                assertFalse(fixture.isEmpty())
            }
            test("has head") {
                assertEquals("item", fixture.firstOrNull())
            }
        }
    }
}