package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.JUnit5Minutests
import com.oneeyedmen.minutest.junit.context
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class ControlPanel(
    val keySwitch: () -> Boolean,
    val beep: () -> Unit,
    val launchMissile: () -> Unit
) {
    fun pressButton() {
        if (keySwitch())
            launchMissile()
        else
            beep()
    }
    val warningLight get() = keySwitch()
}

class CompoundFixtureExampleTests : JUnit5Minutests {

    class Fixture() {
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

    override val tests = context<Fixture> {
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
                keySwitchOn = true
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