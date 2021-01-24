package dev.minutest.examples.experimental

import dev.minutest.experimental.willRun
import dev.minutest.given
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.scenarios.Scenario
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.assertTrue

private fun emptyMutableList() = mutableListOf<String>()

class FlatterScenariosExampleTests : JUnit5Minutests {

    data class Fixture(
        val source: MutableList<String> = emptyMutableList(),
        val destination: MutableList<String> = emptyMutableList()
    )

    fun tests() = rootContext<Fixture>("Moving Between Lists") {

        given {
            Fixture()
        }

        // Where the fixture is set up in the parent context you can just use When and Then to create a self-describing
        // test
        Scenario {

            When("source is populated") {
                source.addAll(listOf("apple", "banana"))
            }

            And("source moveInto destination") {
                source.moveInto(destination)
            }

            Then("destination contains source items") {
                assertEquals(listOf("apple", "banana"), destination)
            }.And("source is empty") {
                assertTrue(source.isEmpty())
            }
        }

        willRun(
            "▾ Moving Between Lists",
            "  ✓ When source is populated, And source moveInto destination," +
                " Then destination contains source items, And source is empty"
        )
    }

}