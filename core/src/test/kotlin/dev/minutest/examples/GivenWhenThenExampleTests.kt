package dev.minutest.examples

import dev.minutest.ContextBuilder
import dev.minutest.TestContextBuilder
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

class ControlPanel2(
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

class GivenWhenThenExampleTests : JUnit5Minutests {

    class Fixture() {
        // Rather than introduce a mocking framework, we can work with
        // functions and mutable state.
        var keySwitchOn = false
        var beeped = false
        var missileLaunched = false

        val controlPanel = ControlPanel2(
            keySwitch = { keySwitchOn },
            beep = { beeped = true },
            launchMissile = { missileLaunched = true }
        )
    }

    fun tests() = rootContext<Fixture> {
        fixture { Fixture() }

        GIVEN("key not turned") {
            // this is a precondition
            test("light is off") {
                assertFalse(controlPanel.warningLight)
            }
            WHEN("pressing the button") {
                controlPanel.pressButton()
            }.THEN("missile wasn't launched") { result ->
                assertFalse(result)
                assertTrue(beeped)
                assertFalse(missileLaunched)
            }
        }


        GIVEN("key turned") {
            modifyFixture {
                keySwitchOn = true
            }
            test("light on") {
                assertTrue(controlPanel.warningLight)
            }
            WHEN("pressing the button") {
                controlPanel.pressButton()
            }.THEN("will launch") { result ->
                assertTrue(result)
                assertFalse(beeped)
                assertTrue(missileLaunched)
            }
        }
    }
}

fun <F> ContextBuilder<F>.GIVEN(name: String, builder: TestContextBuilder<F, F>.() -> Unit) = context("GIVEN $name", builder)

fun <F, R> ContextBuilder<F>.WHEN(name: String, action: (F).() -> R) = WhenClause(this, name, action)


class WhenClause<F, R>(val contextBuilder: ContextBuilder<F>, val name: String, val action: (F).() -> R) {
    fun THEN(thenName: String, block: (F).(R) -> Unit) {
        contextBuilder.test("WHEN $name THEN $thenName") {
            val result = action(this)
            this.block(result)
        }
    }
}