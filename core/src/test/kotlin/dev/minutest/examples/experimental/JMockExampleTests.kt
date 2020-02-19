package dev.minutest.examples.experimental

import com.oneeyedmen.kSera.expecting
import com.oneeyedmen.kSera.invoke
import com.oneeyedmen.kSera.mock
import com.oneeyedmen.kSera.returnValue
import dev.minutest.TestContextBuilder
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.jmock.Mockery

interface Key {
    val isTurned: Boolean
}

interface Beeper {
    fun beep()
}

interface Rocket {
    fun launch()
}

class ControlPanel(
    private val key: Key,
    private val beeper: Beeper,
    private val rocket: Rocket
) {
    fun pressButton() {
        if (key.isTurned)
            rocket.launch()
        else
            beeper.beep()
    }
}

class JMockExampleTests : JUnit5Minutests {

    class Fixture(val mockery: Mockery) {
        val key = mockery.mock<Key>()
        val beeper = mockery.mock<Beeper>()
        val rocket = mockery.mock<Rocket>()

        val controlPanel = ControlPanel(key, beeper, rocket)
    }

    fun tests() = rootContext<Fixture> {

        // jmockFixture manages the mockery lifecycle
        jmockFixture { mockery ->
            Fixture(mockery)
        }

        context("key not turned") {
            modifyFixture {
                mockery.expecting {
                    allowing(key).isTurned.which will returnValue(false)
                }
            }

            test("cannot launch when pressing button") {
                mockery.expecting {
                    oneOf(beeper).beep()
                    never(rocket).launch()
                }
                controlPanel.pressButton()
            }
        }

        context("key turned") {
            modifyFixture {
                mockery.expecting {
                    allowing(key).isTurned.which will returnValue(true)
                }
            }

            test("launches when pressing button") {
                // k-sera has a nice DSL for verify
                mockery {
                    during {
                        controlPanel.pressButton()
                    }
                    verify {
                        never(beeper).beep()
                        oneOf(rocket).launch()
                    }
                }
            }
        }
    }
}

private fun <PF, F> TestContextBuilder<PF, F>.jmockFixture(
    factory: (Unit).(mockery: Mockery) -> F
) {
    // This messing around prevents accidentally sharing the mockery between contexts
    lateinit var mockery: Mockery
    fixture {
        mockery = Mockery()
        factory(mockery)
    }
    after {
        mockery.assertIsSatisfied()
    }
}
