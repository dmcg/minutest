package dev.minutest.examples.scenarios

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.scenarios.Scenario
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

class ControlPanel(
    private val beep: () -> Unit,
    private val launchRocket: () -> Unit
) {
    private var keyTurned: Boolean = false

    fun turnKey() {
        keyTurned = true
    }

    fun pressButton(): Boolean =
        when {
            keyTurned -> {
                launchRocket()
                true
            }
            else -> {
                beep()
                false
            }
        }

    val warningLightOn get() = keyTurned
}

class ScenariosExampleTests : JUnit5Minutests {

    class Fixture {
        var beeped = false
        var launched = false

        val controlPanel = ControlPanel(
            beep = { beeped = true },
            launchRocket = { launched = true }
        )
    }

    fun tests() = rootContext<Fixture> {

        // Scenario defines a nested context
        Scenario("Cannot launch without key switch") {

            // GivenFixture sets up the fixture for the scenario
            GivenFixture("key not turned") {
                Fixture()
            }.Then("warning light is off") {
                // Then can check the setup
                assertFalse(controlPanel.warningLightOn)
            }

            // When performs the operation
            When("pressing the button") {
                controlPanel.pressButton()
            }.Then("result was false") { result ->
                // Then has access to the result
                assertFalse(result)
            }.And("it beeped") {
                // And makes another Thens with checks
                assertTrue(beeped)
            }.And("rocket was not launched") {
                assertFalse(launched)
            }
        }

        Scenario("Will launch with key switch") {
            GivenFixture("key turned") {
                Fixture().apply {
                    controlPanel.turnKey()
                }
            }.Then("warning light is on") {
                assertTrue(controlPanel.warningLightOn)
            }

            When("pressing the button") {
                controlPanel.pressButton()
            }.Then("result was true") { result ->
                assertTrue(result)
            }.And("it didn't beep") {
                assertFalse(beeped)
            }.And("rocket was launched") {
                assertTrue(launched)
            }
        }
    }
}
