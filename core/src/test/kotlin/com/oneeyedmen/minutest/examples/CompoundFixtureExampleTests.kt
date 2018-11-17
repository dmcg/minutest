package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.JupiterTests
import com.oneeyedmen.minutest.junit.context
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class ControlPanel(
    val keySwitch1: () -> Boolean,
    val keySwitch2: () -> Boolean,
    val beep: () -> Unit,
    val launchMissile: () -> Unit
) {
    fun pressButton() {
        if (keySwitch1() && keySwitch2())
            launchMissile()
        else
            beep()
    }
    val warningLight get() = keySwitch1() && keySwitch2()
}

class CompoundFixtureExampleTests : JupiterTests {

    class Fixture() {
        // Rather than introduce a mocking framework, we can work with
        // functions and mutable state.
        var switch1On = false
        var switch2On = false
        var beeped = false
        var missileLaunched = false

        val controlPanel = ControlPanel(
            keySwitch1 = { switch1On },
            keySwitch2 = { switch2On },
            beep = { beeped = true },
            launchMissile = { missileLaunched = true }
        )
    }

    override val tests = context<Fixture> {
        fixture { Fixture() }

        context("no keys turned") {
            modifyFixture {
                switch1On = true
            }
            test("light off") {
                assertFalse(controlPanel.warningLight)
            }
            test("cannot launch") {
                controlPanel.pressButton()
                assertTrue(beeped)
                assertFalse(missileLaunched)
            }
        }

        context("only key 1 turned") {
            modifyFixture {
                switch1On = true
            }
            test("light off") {
                assertFalse(controlPanel.warningLight)
            }
            test("cannot launch") {
                controlPanel.pressButton()
                assertTrue(beeped)
                assertFalse(missileLaunched)
            }
        }

        context("only key 2 turned") {
            // ...
        }

        context("both keys turned") {
            modifyFixture {
                switch1On = true
                switch2On = true
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