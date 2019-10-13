package dev.minutest.examples.experimental

import dev.minutest.TestContextBuilder
import dev.minutest.TestDescriptor
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.assertTrue

class ScenariosExampleTests : JUnit5Minutests {

    fun tests() = rootContext<MutableList<String>> {

        Scenario("adding things to a list") {

            Given("an empty list") { mutableListOf() }

            Then("it is empty") {
                assertTrue(fixture.isEmpty())
            }

            When("you add a thing") {
                add("one")
            }

            Then("then it is in the list") {
                assertEquals(listOf("one"), fixture)
            }
        }

        Scenario("removing things from a list") {

            Given("a list with one thing") { mutableListOf("one") }

            Then("it has the thing") {
                assertEquals(listOf("one"), fixture)
            }

            When("you remove the thing") {
                remove("one")
            }

            Then("it is empty") {
                assertTrue(fixture.isEmpty())
            }
        }
    }
}


fun <PF, F> TestContextBuilder<PF, F>.Scenario(name: String, block: ScenarioBuilder<PF, F>.() -> Unit) =
    ScenarioBuilder<PF, F>(name).apply(block).applyTo(this)

fun <PF, F> ScenarioBuilder<PF, F>.When(name: String, f: F.(testDescriptor: TestDescriptor) -> Unit) {
    this.step("When $name", f)
}

fun <PF, F> ScenarioBuilder<PF, F>.Then(name: String, f: F.(testDescriptor: TestDescriptor) -> Unit) {
    this.step("Then $name", f)
}


data class ScenarioBuilder<PF, F>(
    val name: String
) {

    private var given: (Unit.(testDescriptor: TestDescriptor) -> F)? = null
    private val steps: MutableList<Step<F>> = mutableListOf()

    fun step(name: String, f: F.(testDescriptor: TestDescriptor) -> Unit) {
        steps.add(Step<F>(name, f))
    }

    fun Given(name: String, factory: (Unit).(testDescriptor: TestDescriptor) -> F) {
        given = factory
        step("Given $name") {}
    }


    fun applyTo(contextBuilder: TestContextBuilder<PF, F>) {
        contextBuilder.context(name) {
            given?.let { fixture(it) }

            test(steps.map(Step<F>::name).joinToString()) { testDescriptor: TestDescriptor ->
                steps.forEach { step ->
                    step.f(this, testDescriptor)
                }
            }
        }
    }
}

data class Step<F>(val name: String, val f: F.(testDescriptor: TestDescriptor) -> Unit)