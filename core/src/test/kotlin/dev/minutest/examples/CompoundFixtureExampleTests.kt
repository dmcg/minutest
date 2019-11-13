package dev.minutest.examples

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

class ControlPanel(
    private val beep: () -> Unit,
    private val launchMissile: () -> Unit
) {
    private var keyTurned: Boolean = false

    fun turnKey() {
        keyTurned = true
    }

    fun pressButton() {
        if (keyTurned)
            launchMissile()
        else
            beep()
    }
    val warningLight get() = keyTurned
}

class CompoundFixtureExampleTests : JUnit5Minutests {

    // The fixture consists of all the state affected by tests
    class Fixture() {
        var beeped = false
        var missileLaunched = false

        val controlPanel = ControlPanel(
            beep = { beeped = true },
            launchMissile = { missileLaunched = true }
        )
    }

    fun tests() = rootContext<Fixture> {
        fixture { Fixture() }

        context("key not turned") {
            test("light off") {
                assertFalse(controlPanel.warningLight)
            }
            test("cannot launch") {
                controlPanel.pressButton()
                assertTrue(beeped)
                assertFalse(missileLaunched)
            }
        }

        context("key turned") {
            modifyFixture {
                controlPanel.turnKey()
            }
            test("light on") {
                assertTrue(controlPanel.warningLight)
            }
            test("will launch") {
                controlPanel.pressButton()
                assertFalse(beeped)
                assertTrue(missileLaunched)
            }
        }
    }
}