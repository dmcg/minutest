package dev.minutest.examples.experimental

import dev.minutest.experimental.Scenario
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

internal class ControlPanel(
    val keySwitch: () -> Boolean,
    val beep: () -> Unit,
    val launchMissile: () -> Unit
) {
    fun pressButton(): Boolean {
        return if (keySwitch()) {
            launchMissile()
            true
        } else {
            beep()
            false
        }
    }

    val warningLight get() = keySwitch()
}

class MutableScenariosExampleTests : JUnit5Minutests {

    internal class Fixture() {
        // Rather than introduce a mocking framework, we can work with
        // functions and mutable state.
        var keySwitchOn = false
        var beeped = false
        var missileLaunched = false

        val controlPanel = ControlPanel(
            keySwitch = { keySwitchOn },
            beep = { beeped = true },
            launchMissile = { missileLaunched = true }
        )
    }

    fun tests() = rootContext<Fixture> {

        fixture { Fixture() }

        Scenario("Cannot launch without key switch") {

            Given("key not turned") {
                assertFalse(keySwitchOn)
            }
            Then("warning light is off") {
                assertFalse(controlPanel.warningLight)
            }
            When("pressing the button") {
                controlPanel.pressButton()
            }.Then("result was false") { result ->
                assertFalse(result)
                assertTrue(beeped)
            }.And("missile was not launched") {
                assertFalse(missileLaunched)
            }
        }

        Scenario("Will launch with key switch") {
            Given("key turned") {
                keySwitchOn = true
            }
            Then("warning light is on") {
                assertTrue(controlPanel.warningLight)
            }
            When("pressing the button") {
                controlPanel.pressButton()
            }.Then("result was true") { result ->
                assertTrue(result)
                assertFalse(beeped)
            }.And("missile was launched") {
                assertTrue(missileLaunched)
            }
        }
    }
}
