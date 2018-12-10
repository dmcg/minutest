package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.JUnit5Minutests
import com.oneeyedmen.minutest.rootContext
import org.junit.jupiter.api.Assertions.*

class SubjectUnderTestFixtureExampleTests : JUnit5Minutests {

    override val tests = rootContext<List<String>> {

        context("empty") {
            fixture {
                emptyList()
            }
            test("is empty") {
                // when the fixture is the subject, 'it' reads well
                assertTrue(it.isEmpty())
            }
            test("no head") {
                assertNull(it.firstOrNull())
            }
        }

        // Note that the context name and the fixture state agree
        context("not empty") {
            fixture {
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