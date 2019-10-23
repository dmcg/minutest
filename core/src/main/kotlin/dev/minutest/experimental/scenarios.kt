package dev.minutest.experimental

import dev.minutest.TestContextBuilder

fun <PF, F> TestContextBuilder<PF, F>.Scenario(name: String, block: ScenarioBuilder<PF, F>.() -> Unit) =
    ScenarioBuilder<PF, F>(name).apply(block).applyTo(this)

fun <PF, F> ScenarioBuilder<PF, F>.GivenFixture(name: String, factory: (Unit).() -> F): Thenable<F, F> {
    if (given != null) error("Only one given allowed per scenario")
    given = factory
    step("Given $name") {}
    return Thenable<F, F>()
}

fun <PF, F> ScenarioBuilder<PF, F>.Given(name: String, operation: F.() -> Unit) {
    befores.add(operation)
    step("Given $name") {}
}

fun <PF, F, R> ScenarioBuilder<PF, F>.When(name: String, f: F.() -> R): Thenable<F, R> {
    this.step("When $name", f)
    return Thenable<F, R>()
}

fun <PF, F> ScenarioBuilder<PF, F>.Then(name: String, f: F.() -> Unit) {
    this.step("Then $name", f)
}

class ScenarioBuilder<PF, F>(
    val name: String
) {
    internal var given: (Unit.() -> F)? = null
    internal val befores: MutableList<(F) -> Unit> = mutableListOf()

    private val steps: MutableList<Step<F, *>> = mutableListOf()

    internal fun <R> step(name: String, f: F.() -> R) {
        steps.add(Step(name, f))
    }

    internal fun applyTo(contextBuilder: TestContextBuilder<PF, F>) {
        contextBuilder.context(name) {
            given?.let { fixture { it() }  }
            befores.forEach { item -> before { item(this) } }

            test(steps.map(Step<F, *>::name).joinToString()) {
                steps.forEach { step ->
                    step.f(this)
                }
            }
        }
    }
}

// My idea here is to have a composite step. When adds itself as a step, and if we then do When.Then
// that adds a composite step that runs the When and feeds the result into the Then.
// Then prune the first When step from the steps by having a id passed from the first it into subsequent
// steps and reducing the steps by keeping only the last step with a given id.

internal class Step<F, R>(val name: String, val f: F.() -> R)

class Thenable<F, R> {
    fun Then(name: String, f: F.(R) -> Unit) {

    }
}