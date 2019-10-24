package dev.minutest.experimental

import dev.minutest.TestContextBuilder

fun <PF, F> TestContextBuilder<PF, F>.Scenario(name: String, block: ScenarioBuilder<PF, F>.() -> Unit) =
    ScenarioBuilder<PF, F>(name).apply(block).applyToContext(this)

fun <PF, F> ScenarioBuilder<PF, F>.GivenFixture(name: String, factory: (Unit).() -> F): Thenable<F, F> {
    val next = Thenable<F, F>(this)
    preambles.add(
        Preamble("Given $name") {
            fixture { factory().also { next.previousResult = it } }
        }
    )
    return next

}

fun <PF, F> ScenarioBuilder<PF, F>.Given(name: String, operation: F.() -> Unit) {
    preambles.add(
        Preamble("Given $name") {
            modifyFixture { operation() }
        }
    )
}

fun <PF, F, R> ScenarioBuilder<PF, F>.When(name: String, f: F.() -> R): Thenable<F, R> {
    val next = Thenable<F, R>(this)
    steps.add(
        TestStep<F, R>("When $name") {
            f().also {
                next.previousResult = it
            }
        }
    )
    return next
}

fun <PF, F> ScenarioBuilder<PF, F>.Then(name: String, f: F.() -> Unit) {
    steps.add(
        TestStep("Then $name", f)
    )
}

internal class Preamble<PF, F>(
    val name: String,
    val f: (TestContextBuilder<PF, F>).() -> Unit
)

internal class TestStep<F, R>(
    val name: String,
    val f: (F).() -> R
)

class Thenable<F, R>(val scenarioBuilder: ScenarioBuilder<*, F>) {
    private val previousResultHolder = mutableListOf<R>()

    internal var previousResult  get() = previousResultHolder.first()
        set(value) { previousResultHolder.add(value) }

    fun Then(name: String, f: F.(R) -> Unit) {
        scenarioBuilder.steps.add(
            TestStep<F, Unit>("Then $name") {
                this.f(previousResult)
            }
        )
    }
}

class ScenarioBuilder<PF, F>(
    val name: String
) {
    internal val preambles: MutableList<Preamble<F, F>> = mutableListOf()
    internal val steps: MutableList<TestStep<F, *>> = mutableListOf()

    internal fun applyToContext(contextBuilder: TestContextBuilder<PF, F>) {
        contextBuilder.context(name) {
            val newContext = this
            preambles.forEach { preamble ->
                preamble.f(newContext)
            }
            val testName = (preambles.map { it.name } + steps.map { it.name }).joinToString()
            test(testName) {
                steps.forEach { step ->
                    step.f(this)
                }
            }
        }
    }
}