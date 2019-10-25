package dev.minutest.examples.experimental

import dev.minutest.experimental.GivenFixture
import dev.minutest.experimental.Scenario
import dev.minutest.experimental.When
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.assertTrue
import dev.minutest.experimental.checkedAgainst as willRun


class FixtureScenariosExampleTests : JUnit5Minutests {

    fun tests() = rootContext<MutableList<String>>("Mutable Lists") {

        Scenario("adding things to a list") {

            GivenFixture("an empty list") {
                mutableListOf()
            }.Then("it is empty") {
                assert(it.isEmpty())
            }

            When("you add a thing") {
                add("thing")
            }.Then("result is true") { result ->
                assertTrue(result)
            }.And("thing is in the list") {
                assertEquals(listOf("thing"), fixture)
            }
        }

        Scenario("removing things from a list") {

            GivenFixture("a list with one thing") {
                mutableListOf("thing")
            }.Then("it has the thing") {
                assertEquals(listOf("thing"), it)
            }

            When("you remove the thing") {
                remove("thing")
            }.Then("result is true") { result ->
                assertTrue(result)
            }.And("the list is empty") {
                assertTrue(fixture.isEmpty())
            }
        }

        // Minutest will check that the following tests are run
        willRun(
            "Mutable Lists",
            "  adding things to a list",
            "    Given an empty list, Then it is empty, When you add a thing, Then result is true, And thing is in the list",
            "  removing things from a list",
            "    Given a list with one thing, Then it has the thing, When you remove the thing, Then result is true, And the list is empty"
        )
    }
}