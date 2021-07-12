package dev.minutest.scenarios

import dev.minutest.rootContext
import dev.minutest.testing.runTests
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class ScenarioErrorReportingTests {

    @Test
    fun `reports errors in steps`() {
        val tests = rootContext<String> {
            Scenario("Something goes wrong in the middle") {
                GivenFixture("the string banana") {
                    "banana"
                }
                Then("the fixture is banana") {
                    assertEquals("banana", this)
                }
                And("it has 999 chars") {
                    assertEquals(999, this.length)
                }
                And("the Pope is Catholic") {
                    assertTrue(true)
                }
            }
        }
        val error = runTests(tests).first()
        assertEquals(
            listOf(
                "",
                "Error in Something goes wrong in the middle",
                "✓ Given the string banana",
                "✓ Then the fixture is banana",
                "X And it has 999 chars",
                "expected: <999> but was: <6>",
                "- And the Pope is Catholic"
            ).joinToString("\n"),
            error.message
        )
    }

    @Test
    fun `reports errors in preamble`() {
        val tests = rootContext<String> {
            Scenario("Something goes wrong in the preamble") {
                GivenFixture("the string banana") {
                    fail("oops")
                }
                Then("the fixture is banana") {
                    assertEquals("banana", this)
                }
            }
        }
        val error = runTests(tests).first()
        assertEquals(
            listOf(
                "",
                "Error in Something goes wrong in the preamble",
                "X Given the string banana",
                "oops",
                "- Then the fixture is banana"
            ).joinToString("\n"),
            error.message
        )
    }

    @Test
    fun `reports errors in preamble And`() {
        val tests = rootContext<String> {
            Scenario("Something goes wrong in the preamble") {
                GivenFixture("the string banana") {
                    "banana"
                }.And("something goes wrong") {
                    fail("oops")
                }
                Then("the fixture is banana") {
                    assertEquals("banana", this)
                }
            }
        }
        val error = runTests(tests).first()
        assertEquals(
            listOf(
                "",
                "Error in Something goes wrong in the preamble",
                "✓ Given the string banana",
                "X And something goes wrong",
                "oops",
                "- Then the fixture is banana"
            ).joinToString("\n"),
            error.message
        )
    }

}