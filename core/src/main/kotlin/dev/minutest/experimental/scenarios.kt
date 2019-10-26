@file:Suppress("FunctionName")

package dev.minutest.experimental

import dev.minutest.MinutestFixture
import dev.minutest.TestContextBuilder

fun <PF, F> TestContextBuilder<PF, F>.Scenario(description: String, block: ScenarioBuilder<PF, F>.() -> Unit) =
    ScenarioBuilder<PF, F>(description).apply(block).applyToContext(this)

@MinutestFixture
class ScenarioBuilder<PF, F>(
    private val description: String
) {
    private val preambles: MutableList<Preamble<F, F>> = mutableListOf()
    private val steps: MutableList<TestStep<F, *>> = mutableListOf()

    fun GivenFixture(description: String, factory: (Unit).() -> F): Givens<F, F> {
        val next = Givens<F, F>(this)
        addPreamble(
            Preamble("Given $description") {
                fixture { factory().also { next.result = it } }
            }
        )
        return next
    }

    fun <R> Given(description: String, operation: F.() -> R): Thens<F, R> {
        val next = Thens<F, R>(this)
        addPreamble(
            Preamble("Given $description") {
                modifyFixture { operation().also { next.result = it } }
            }
        )
        return next
    }

    fun <R> When(description: String, f: F.() -> R): Whens<F, R> {
        val next = Whens<F, R>(this)
        addStep(
            TestStep<F, R>("When $description") {
                f().also {
                    next.result = it
                }
            }
        )
        return next
    }

    fun Then(description: String, f: F.() -> Unit): Thens<F, Unit> {
        addStep(
            TestStep("Then $description", f)
        )
        return Thens(this)
    }

    fun And(description: String, f: F.() -> Unit) {
        addStep(
            TestStep("And $description", f)
        )
    }

    /**
     * Name the fixture to improve communication.
     */
    val F.fixture: F get() = this

    internal fun addPreamble(preamble: Preamble<F, F>) {
        preambles.add(preamble)
    }

    internal fun addStep(step: TestStep<F, *>) {
        steps.add(step)
    }

    internal fun applyToContext(contextBuilder: TestContextBuilder<PF, F>) {
        contextBuilder.context(description) {
            val newContext = this
            this@ScenarioBuilder.preambles.forEach { preamble ->
                preamble.f(newContext)
            }
            val testName = (this@ScenarioBuilder.preambles.map { it.description } + this@ScenarioBuilder.steps.map { it.description }).joinToString()
            test(testName) {
                this@ScenarioBuilder.steps.forEach { step ->
                    step.f(this)
                }
            }
        }
    }
}

class Givens<F, R>(private val scenarioBuilder: ScenarioBuilder<*, F>) {
    private val resultHolder = mutableListOf<R>()

    internal  var result  get() = resultHolder.first()
        set(value) { resultHolder.add(value) }

    fun <R2> And(description: String, operation: F.() -> R2): Givens<F, R2> {
        val next = Givens<F, R2>(scenarioBuilder)
        scenarioBuilder.addPreamble(
            Preamble("And $description") {
                modifyFixture { operation().also { next.result = it } }
            }
        )
        return next
    }

    fun Then(description: String, prefix: String = "Then", f: F.(previousResult: R) -> Unit): Thens<F, R> {
        scenarioBuilder.addStep(
            TestStep<F, Unit>("$prefix $description") {
                this.f(result)
            }
        )
        return Thens(scenarioBuilder, resultHolder)
    }
}

class Whens<F, R>(private val scenarioBuilder: ScenarioBuilder<*, F>) {
    private val resultHolder = mutableListOf<R>()

    internal  var result  get() = resultHolder.first()
        set(value) { resultHolder.add(value) }

    fun <R2> And(description: String, f: F.() -> R2): Whens<F, R2> {
        val next = Whens<F, R2>(scenarioBuilder)
        scenarioBuilder.addStep(
            TestStep<F, R2>("When $description") {
                f().also {
                    next.result = it
                }
            }
        )
        return next
    }

    fun Then(description: String, prefix: String = "Then", f: F.(previousResult: R) -> Unit): Thens<F, R> {
        scenarioBuilder.addStep(
            TestStep<F, Unit>("$prefix $description") {
                this.f(result)
            }
        )
        return Thens(scenarioBuilder, resultHolder)
    }
}

class Thens<F, R>(
    private val scenarioBuilder: ScenarioBuilder<*, F>,
    private val resultHolder: MutableList<R> = mutableListOf()
) {
    internal var result  get() = resultHolder.first()
        set(value) { resultHolder.add(value) }

    fun Then(description: String, prefix: String = "Then", f: F.(previousResult: R) -> Unit): Thens<F, R> {
        scenarioBuilder.addStep(
            TestStep<F, Unit>("$prefix $description") {
                this.f(result)
            }
        )
        return this
    }
    fun And(description: String, f: F.(previousResult: R) -> Unit): Thens<F, R> = Then(description, "And", f)
}

internal data class Preamble<PF, F>(
    val description: String,
    val f: (TestContextBuilder<PF, F>).() -> Unit
)

internal data class TestStep<F, R>(
    val description: String,
    val f: (F).() -> R
)