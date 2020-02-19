package dev.minutest.examples.experimental

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

    fun pressButton(): Boolean {
        return if (keyTurned) {
            launchRocket()
            true
        } else {
            beep()
            false
        }
    }

    val warningLightOn get() = keyTurned
}

class MutableScenariosExampleTests : JUnit5Minutests {

    // The fixture consists of all the state affected by tests
    class Fixture() {
        var beeped = false
        var launched = false

        val controlPanel = ControlPanel(
            beep = { beeped = true },
            launchRocket = { launched = true }
        )
    }

    fun tests() = rootContext<Fixture> {

        Scenario("Cannot launch without key switch") {
            GivenFixture("key not turned") {
                Fixture()
            }.Then("warning light is off") {
                assertFalse(controlPanel.warningLightOn)
            }
            When("pressing the button") {
                controlPanel.pressButton()
            }.Then("result was false") { result ->
                assertFalse(result)
            }.And("it beeped") {
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
            }
            Then("warning light is on") {
                assertTrue(controlPanel.warningLightOn)
            }
            When("pressing the button") {
                controlPanel.pressButton()
            }.Then("result was true") { result ->
                assertTrue(result)
            }.And("it didn't beep") {
                assertFalse(beeped)
            }.And("missile was launched") {
                assertTrue(launched)
            }
        }
    }
}
