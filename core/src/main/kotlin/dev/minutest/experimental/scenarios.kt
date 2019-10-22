package dev.minutest.experimental

import dev.minutest.TestContextBuilder
import dev.minutest.TestDescriptor

fun <PF, F> TestContextBuilder<PF, F>.Scenario(name: String, block: ScenarioBuilder<PF, F>.() -> Unit) =
    ScenarioBuilder<PF, F>(name).apply(block).applyTo(this)

fun <PF, F> ScenarioBuilder<PF, F>.Given_(name: String, factory: (Unit).(testDescriptor: TestDescriptor) -> F) {
    if (given != null) error("Only one given allowed per scenario")
    given = factory
    step("Given $name") {}
}

fun <PF, F> ScenarioBuilder<PF, F>.Given(name: String, operation: F.(testDescriptor: TestDescriptor) -> Unit) {
    befores.add(operation)
    step("Given $name") {}
}

fun <PF, F> ScenarioBuilder<PF, F>.When(name: String, f: F.(testDescriptor: TestDescriptor) -> Unit) {
    this.step("When $name", f)
}

fun <PF, F> ScenarioBuilder<PF, F>.Then(name: String, f: F.(testDescriptor: TestDescriptor) -> Unit) {
    this.step("Then $name", f)
}

class ScenarioBuilder<PF, F>(
    val name: String
) {
    internal var given: (Unit.(testDescriptor: TestDescriptor) -> F)? = null
    internal val befores: MutableList<(F, TestDescriptor) -> Unit> = mutableListOf()

    private val steps: MutableList<Step<F>> = mutableListOf()

    internal fun step(name: String, f: F.(testDescriptor: TestDescriptor) -> Unit) {
        steps.add(Step(name, f))
    }

    internal fun applyTo(contextBuilder: TestContextBuilder<PF, F>) {
        contextBuilder.context(name) {
            given?.let { fixture(it) }
            befores.forEach { before(it) }

            test(steps.map(Step<F>::name).joinToString()) { testDescriptor: TestDescriptor ->
                steps.forEach { step ->
                    step.f(this, testDescriptor)
                }
            }
        }
    }
}



internal data class Step<F>(val name: String, val f: F.(testDescriptor: TestDescriptor) -> Unit)