@file:Suppress("FunctionName")

package dev.minutest.experimental

import dev.minutest.ContextBuilder
import dev.minutest.MinutestFixture
import dev.minutest.TestContextBuilder
import dev.minutest.experimental.StepType.*

fun <F> ContextBuilder<F>.Scenario(description: String, block: ScenarioBuilder<F>.() -> Unit) =
    ScenarioBuilder<F>(description).apply(block).applyToContext(this)

@MinutestFixture
class ScenarioBuilder<F>(
    private val description: String
) {
    private val preambles: MutableList<Preamble> = mutableListOf()
    private val steps: MutableList<TestStep<F, *>> = mutableListOf()

    fun GivenFixture(description: String, factory: (Unit).() -> F): Givens<F, F> {
        val next = Givens<F, F>(this)
        lateinit var gah: Preamble
        preambles.add(
            Preamble("Given $description") {
                fixture {
                    this@ScenarioBuilder.tryThrowingScenarioFailedException(gah) {
                        factory().also { next.result = it }
                    }
                }
            }.also { gah = it }
        )
        return next
    }

    fun <R> Given(description: String, prefix: String = "Given", operation: F.() -> R): Givens<F, R> {
        val next = Givens<F, R>(this)
        lateinit var gah: Preamble
        preambles.add(
            Preamble("$prefix $description") {
                modifyFixture {
                    this@ScenarioBuilder.tryThrowingScenarioFailedException(gah) {
                        operation().also { next.result = it }
                    }
                }
            }.also { gah = it }
        )
        return next
    }

    fun <R> When(description: String, f: F.() -> R): Whens<F, R> {
        val next = Whens<F, R>(this)
        addStep(
            TestStep<F, R>("When $description", WHEN) {
                f().also {
                    next.result = it
                }
            }
        )
        return next
    }

    fun Then(description: String, f: F.() -> Unit): Thens<F> {
        addStep(
            TestStep("Then $description", THEN, f)
        )
        return Thens(this)
    }

    fun And(description: String, f: F.() -> Unit) {
        addStep(
            TestStep("And $description", INFER, f)
        )
    }

    /**
     * Name the fixture to improve communication.
     */
    val F.fixture: F get() = this

    internal fun addStep(step: TestStep<F, *>) {
        steps.add(if (step.type == INFER) step.copy(type = steps.lastOrNull()?.type ?: WHEN) else step)
    }

    internal fun applyToContext(contextBuilder: ContextBuilder<F>) {
        contextBuilder.context(description) {
            val newContext = this
            val scenarioBuilder = this@ScenarioBuilder
            scenarioBuilder.preambles.forEach { preamble ->
                preamble.f(newContext)
            }
            test(scenarioBuilder.testName) {
                scenarioBuilder.steps.forEach { step ->
                    scenarioBuilder.tryThrowingScenarioFailedException(step) {
                        step.f(this)
                    }
                }
            }
        }
    }

    private val testName: String
        get() = (
            preambles.map { it.description } +
                steps.map { it.toTestNameComponent() }
            ).joinToString()

    inner class Preamble(
        val description: String,
        val f: (TestContextBuilder<F, F>).() -> Unit
    ) {
        override fun toString() = description
    }

    private fun <R> tryThrowingScenarioFailedException(step: Any, t2: () -> R): R =
        try {
            t2()
        } catch (t: Throwable) {
            throw scenarioFailedExceptionFor(description, preambles, steps, step, t)
        }
}

class Givens<F, R>(private val scenarioBuilder: ScenarioBuilder<F>) {
    private val resultHolder = mutableListOf<R>()

    internal var result
        get() = resultHolder.first()
        set(value) {
            resultHolder.add(value)
        }

    fun <R2> And(description: String, operation: F.() -> R2): Givens<F, R2> {
        return scenarioBuilder.Given(description, "And", operation)
    }

    fun Then(description: String, prefix: String = "Then", f: F.(previousResult: R) -> Unit): ResultingThens<F, R> {
        scenarioBuilder.addStep(
            TestStep<F, Unit>("$prefix $description", THEN) {
                this.f(result)
            }
        )
        return ResultingThens(scenarioBuilder, resultHolder)
    }
}

class Whens<F, R>(private val scenarioBuilder: ScenarioBuilder<F>) {
    private val resultHolder = mutableListOf<R>()

    internal var result
        get() = resultHolder.first()
        set(value) {
            resultHolder.add(value)
        }

    fun <R2> And(description: String, f: F.() -> R2): Whens<F, R2> {
        val next = Whens<F, R2>(scenarioBuilder)
        scenarioBuilder.addStep(
            TestStep<F, R2>("And $description", WHEN) {
                f().also {
                    next.result = it
                }
            }
        )
        return next
    }

    fun Then(description: String, prefix: String = "Then", f: F.(previousResult: R) -> Unit): ResultingThens<F, R> {
        scenarioBuilder.addStep(
            TestStep<F, Unit>("$prefix $description", THEN) {
                this.f(result)
            }
        )
        return ResultingThens(scenarioBuilder, resultHolder)
    }
}

class ResultingThens<F, R>(
    private val scenarioBuilder: ScenarioBuilder<F>,
    private val resultHolder: MutableList<R> = mutableListOf()
) {
    internal var result
        get() = resultHolder.first()
        set(value) {
            resultHolder.add(value)
        }

    fun Then(description: String, prefix: String = "Then", f: F.(previousResult: R) -> Unit): ResultingThens<F, R> {
        scenarioBuilder.addStep(
            TestStep<F, Unit>("$prefix $description", THEN) {
                this.f(result)
            }
        )
        return this
    }

    fun And(description: String, f: F.(previousResult: R) -> Unit): ResultingThens<F, R> = Then(description, "And", f)
}

class Thens<F>(
    private val scenarioBuilder: ScenarioBuilder<F>
) {
    fun Then(description: String, prefix: String = "Then", f: F.() -> Unit): Thens<F> {
        scenarioBuilder.addStep(
            TestStep<F, Unit>("$prefix $description", THEN) {
                this.f()
            }
        )
        return this
    }

    fun And(description: String, f: F.() -> Unit): Thens<F> = Then(description, "And", f)
}

internal data class TestStep<F, R>(
    val description: String,
    val type: StepType,
    val f: (F).() -> R
) {
    override fun toString() = description
}

class ScenarioStepFailedException(message: String, cause: Throwable) : Error(message, cause)

internal enum class StepType { WHEN, THEN, INFER }

private fun scenarioFailedExceptionFor(
    scenarioDescription: String,
    preambles: List<ScenarioBuilder<*>.Preamble>,
    steps: List<TestStep<*, *>>,
    current: Any,
    t: Throwable
) = ScenarioStepFailedException(
    (listOf("", "Error in $scenarioDescription") + stepLinesFor(preambles, steps, current, t.message)).joinToString("\n")
    , t)

private fun stepLinesFor(preambles: List<ScenarioBuilder<*>.Preamble>, steps: List<TestStep<*, *>>, current: Any, errorMessage: String?): List<String> {
    val all = preambles + steps
    val currentIndex = all.indexOf(current)
    val before = all.subList(0, currentIndex).map { "✓ $it" }
    val currentLine = "X " + all[currentIndex].toString()
    val after = all.subList(currentIndex + 1, all.size).map { "- $it" }
    return (before +
        currentLine +
        errorMessage +
        after).filterNotNull()
}

private fun TestStep<*, *>.toTestNameComponent() = when {
    type == THEN && description.startsWith("And") -> "And…"
    type == THEN -> "Then…"
    else -> description
}

