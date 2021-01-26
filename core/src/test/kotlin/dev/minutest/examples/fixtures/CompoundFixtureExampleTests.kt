package dev.minutest.examples.fixtures

import dev.minutest.beforeEach
import dev.minutest.given
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test
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

    fun pressButton() {
        if (keyTurned)
            launchRocket()
        else
            beep()
    }
    val warningLightOn get() = keyTurned
}

class CompoundFixtureExampleTests : JUnit5Minutests {

    // The fixture consists of all the state affected by tests
    class Fixture {
        var beeped = false
        var launched = false

        val controlPanel = ControlPanel(
            beep = { beeped = true },
            launchRocket = { launched = true }
        )
    }

    fun tests() = rootContext<Fixture> {
        given { Fixture() }

        context("key not turned") {
            test("light is off") {
                assertFalse(controlPanel.warningLightOn)
            }
            test("cannot launch when pressing button") {
                controlPanel.pressButton()
                assertTrue(beeped)
                assertFalse(launched)
            }
        }

        context("key turned") {
            beforeEach {
                controlPanel.turnKey()
            }
            test("light is on") {
                assertTrue(controlPanel.warningLightOn)
            }
            test("launches when pressing button") {
                controlPanel.pressButton()
                assertFalse(beeped)
                assertTrue(launched)
            }
        }
    }
}