package dev.minutest.examples.experimental

import dev.minutest.experimental.Scenario
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.assertTrue
import dev.minutest.experimental.checkedAgainst as willRun

private fun emptyMutableList() = mutableListOf<String>()

fun <T> MutableCollection<T>.moveInto(destination: MutableCollection<T>): Boolean =
    destination.addAll(this).also {
        clear()
    }


class ScenariosExampleTests : JUnit5Minutests {

    data class Fixture(
        val source: MutableList<String> = emptyMutableList(),
        val destination: MutableList<String> = emptyMutableList()
    )

    // A feature is a context
    fun tests() = rootContext<Fixture>("Moving Between Lists") {

        // we can populate the fixture as usual
        fixture {
            Fixture()
        }

        // Scenario defines a nested context
        Scenario("Moving around items") {

            // Given sets up the fixture for the scenario
            Given("an empty destination") {
                destination.clear()
            }.And("a populated source") {
                source.addAll(listOf("apple", "banana"))
            }

            // When is for operations
            When("source moveInto destination") {
                source.moveInto(destination)
            }

            // Then is for checks
            Then("destination contains source items") {
                assertEquals(listOf("apple", "banana"), destination)
            }.And("source is empty") {
                // Chained Ands continue the statement
                assertTrue(source.isEmpty())
            }

            // You can have more Whens
            When("moving back") {
                destination.moveInto(source)
            }.Then("result is true") { result ->
                // Chained Then's have the result of the previous block
                assertTrue(result)
            }

            // You can have standalone Ands
            And("they have swapped again") {
                assertTrue(destination.isEmpty())
                assertEquals(listOf("apple", "banana"), source)
            }
        }

        // Minutest will check that the following tests are run - note that it is one long test name
        willRun(
            "Moving Between Lists",
            "  Moving around items",
            "    Given an empty destination, And a populated source," +
                " When source moveInto destination," +
                " When moving back"
        )
    }

}

class ScenarioTableTests : JUnit5Minutests {

    fun tests() = rootContext<ScenariosExampleTests.Fixture>("Moving Between Lists") {
        val lists: List<MutableList<String>> = listOf(
            mutableListOf(),
            mutableListOf("apple"),
            mutableListOf("banana", "cumcumber")
        )
        val things: List<Pair<MutableList<String>, MutableList<String>>> = combinationsOf(lists, lists)

        // Scenario defines a nested context
        things.forEach { (originalSource, originalDestination) ->
            val fixture = ScenariosExampleTests.Fixture(originalSource.toMutableList(), originalDestination.toMutableList())
            Scenario("Moving ${originalSource} to ${originalDestination}") {
                GivenFixture("$originalSource to $originalDestination") {
                    fixture
                }

                // When is for operations
                When("$originalSource moveInto $originalDestination") {
                    source.moveInto(destination)
                }.Then("result is ${originalSource.isNotEmpty()}") { result ->
                    assertEquals(result, originalSource.isNotEmpty())
                }.And("destination contains $originalSource") {
                    assertTrue(destination.containsAll(originalSource))
                }.And("destination contains $originalDestination") {
                    assertTrue(destination.containsAll(originalDestination))
                }.And("source is empty") {
                    assertTrue(source.isEmpty())
                }
            }
        }
    }
}

fun <T, U> combinationsOf(ts: Iterable<T>, us: Iterable<U>): List<Pair<T, U>> = ts.flatMap { t ->
    us.map { u -> t to u }
}