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

fun <PF, F> ScenarioBuilder<PF, F>.Then(name: String, f: F.() -> Unit): Andable<F> {
    steps.add(
        TestStep("Then $name", f)
    )
    return Andable(this)
}

internal class Preamble<PF, F>(
    val name: String,
    val f: (TestContextBuilder<PF, F>).() -> Unit
)

internal class TestStep<F, R>(
    val name: String,
    val f: (F).() -> R
)

class Thenable<F, R>(private val scenarioBuilder: ScenarioBuilder<*, F>) {
    private val previousResultHolder = mutableListOf<R>()

    internal var previousResult  get() = previousResultHolder.first()
        set(value) { previousResultHolder.add(value) }

    fun Then(name: String, f: F.(previousResult: R) -> Unit): Andable<F> {
        scenarioBuilder.steps.add(
            TestStep<F, Unit>("Then $name") {
                this.f(previousResult)
            }
        )
        return Andable(scenarioBuilder)
    }
    fun <R2> And(name: String, f: F.() -> R2): Thenable<F, R2> {
        val next = Thenable<F, R2>(scenarioBuilder)
        scenarioBuilder.steps.add(
            TestStep<F, R2>("And $name") {
                f().also {
                    next.previousResult = it
                }
            }
        )
        return next
    }
}

class Andable<F>(val scenarioBuilder: ScenarioBuilder<*, F>) {
    fun And(name: String, f: F.() -> Unit): Andable<F> {
        scenarioBuilder.steps.add(
            TestStep<F, Unit>("And $name") {
                this.f()
            }
        )
        return Andable(scenarioBuilder)
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