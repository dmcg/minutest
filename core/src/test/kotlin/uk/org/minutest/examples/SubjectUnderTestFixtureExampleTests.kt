package uk.org.minutest.examples

import org.junit.jupiter.api.Assertions.*
import uk.org.minutest.junit.JUnit5Minutests
import uk.org.minutest.rootContext

class SubjectUnderTestFixtureExampleTests : JUnit5Minutests {

    override val tests = rootContext<List<String>> {

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