package dev.minutest.examples.experimental

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.scenarios.Scenario
import org.junit.jupiter.api.Assertions
import kotlin.test.assertTrue


private fun emptyMutableList() = mutableListOf<String>()

class ScenarioTableTests : JUnit5Minutests {


    data class Fixture(
        val source: MutableList<String> = emptyMutableList(),
        val destination: MutableList<String> = emptyMutableList()
    )

    fun tests() = rootContext<Fixture>("Moving Between Lists") {

        val lists: List<MutableList<String>> = listOf(
            mutableListOf(),
            mutableListOf("apple"),
            mutableListOf("banana", "cucumber")
        )
        val things: List<Pair<MutableList<String>, MutableList<String>>> = combinationsOf(lists, lists)

        things.forEach { (originalSource, originalDestination) ->
            val fixture = Fixture(originalSource.toMutableList(), originalDestination.toMutableList())
            Scenario("Moving ${originalSource} to ${originalDestination}") {
                GivenFixture("$originalSource to $originalDestination") {
                    fixture
                }
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