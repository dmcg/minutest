package dev.minutest.examples.experimental

import dev.minutest.experimental.Scenario
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions
import kotlin.test.assertTrue

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
                    Assertions.assertEquals(result, originalSource.isNotEmpty())
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