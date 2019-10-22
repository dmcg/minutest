package dev.minutest.examples.experimental

import dev.minutest.experimental.Given_
import dev.minutest.experimental.Scenario
import dev.minutest.experimental.Then
import dev.minutest.experimental.When
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.assertTrue
import dev.minutest.experimental.checkedAgainst as willRun


class ScenariosExampleTests : JUnit5Minutests {

    fun tests() = rootContext<MutableList<String>>("Mutable Lists") {

        Scenario("adding things to a list") {

            Given_("an empty list") { mutableListOf() }

            Then("it is empty") {
                assertTrue(fixture.isEmpty())
            }

            When("you add a thing") {
                add("one")
            }

            Then("then it is in the list") {
                assertEquals(listOf("one"), fixture)
            }
        }

        Scenario("removing things from a list") {

            Given_("a list with one thing") { mutableListOf("one") }

            Then("it has the thing") {
                assertEquals(listOf("one"), fixture)
            }

            When("you remove the thing") {
                remove("one")
            }

            Then("it is empty") {
                assertTrue(fixture.isEmpty())
            }
        }

        // Minutest will check that the following tests are run
        willRun(
            "Mutable Lists",
            "  adding things to a list",
            "    Given an empty list, Then it is empty, When you add a thing, Then then it is in the list",
            "  removing things from a list",
            "    Given a list with one thing, Then it has the thing, When you remove the thing, Then it is empty"
        )
    }
}