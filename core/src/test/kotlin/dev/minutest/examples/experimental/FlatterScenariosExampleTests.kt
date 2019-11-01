package dev.minutest.examples.experimental

import dev.minutest.experimental.Scenario
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.assertTrue
import dev.minutest.experimental.checkedAgainst as willRun

private fun emptyMutableList() = mutableListOf<String>()

class FlatterScenariosExampleTests : JUnit5Minutests {

    data class Fixture(
        val source: MutableList<String> = emptyMutableList(),
        val destination: MutableList<String> = emptyMutableList()
    )

    // A feature is a context
    fun tests() = rootContext<Fixture>("Moving Between Lists") {

        fixture {
            Fixture()
        }

        Scenario("Moving around items") {

            When("source is populated") {
                source.addAll(listOf("apple", "banana"))
            }

            When("source moveInto destination") {
                source.moveInto(destination)
            }

            Then("destination contains source items") {
                assertEquals(listOf("apple", "banana"), destination)
            }.And("source is empty") {
                // Chained Ands continue the statement
                assertTrue(source.isEmpty())
            }
        }

        willRun(
            "Moving Between Lists",
            "  Moving around items"
        )
    }

}