@file:Suppress("FunctionName")

package dev.minutest.scenarios

import dev.minutest.ContextBuilder
import dev.minutest.MinutestFixture
import dev.minutest.scenarios.StepType.*

fun <F> ContextBuilder<F>.Scenario(description: String? = null, block: ScenarioBuilder<F>.() -> Unit) =
    ScenarioBuilder<F>(description).apply(block).applyToContext(this)

@MinutestFixture
class ScenarioBuilder<F>(
    private val description: String?
) {
    private val preambles: MutableList<Preamble<F>> = mutableListOf()
    private val steps: MutableList<TestStep<F, *>> = mutableListOf()

    fun GivenFixture(description: String, factory: (Unit).() -> F): Givens<F, F> {
        val next = Givens<F, F>(this)
        lateinit var gah: Preamble<F>
        preambles.add(
            Preamble<F>("Given $description") {
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
        lateinit var gah: Preamble<F>
        preambles.add(
            Preamble<F>("$prefix $description") {
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
        steps.add(
            if (step.type == INFER)
                step.copy(type = steps.lastOrNull()?.type ?: WHEN)
            else
                step
        )
    }

    internal fun applyToContext(contextBuilder: ContextBuilder<F>) {
        val scenarioBuilder = this@ScenarioBuilder
        val topLevelName = description ?: generateName()
        if (preambles.isNotEmpty()) {
            contextBuilder.context(topLevelName) {
                val newContext = this
                scenarioBuilder.preambles.forEach { preamble ->
                    preamble.f(newContext)
                }
                scenarioBuilder.addTestForStepsTo(this,
                    testName = if (scenarioBuilder.description == null) "test" else scenarioBuilder.generateName()
                )
            }
        } else {
            scenarioBuilder.addTestForStepsTo(contextBuilder, testName = topLevelName)
        }
    }

    private fun addTestForStepsTo(contextBuilder: ContextBuilder<F>, testName: String) {
        contextBuilder.test(testName) {
            steps.forEach { step ->
                tryThrowingScenarioFailedException(step) {
                    step.f(this)
                }
            }
        }
    }

    private fun <R> tryThrowingScenarioFailedException(step: Any, f: () -> R): R =
        try {
            f()
        } catch (t: Throwable) {
            throw scenarioFailedExceptionFor(description ?: generateName(), preambles, steps, step, t)
        }

    private fun generateName(): String {
        val candidate = (preambles.map { it.description } + steps.map { it.description }).joinToString()
        return when {
            candidate.length <= 128 -> candidate
            else -> (preambles.map { it.description } + steps.map { it.toElidedTestNameComponent() }).joinToString()
        }
    }
}

private fun scenarioFailedExceptionFor(
    scenarioDescription: String,
    preambles: List<Preamble<*>>,
    steps: List<TestStep<*, *>>,
    current: Any,
    t: Throwable
) = ScenarioStepFailedException(
    (listOf("", "Error in $scenarioDescription") + stepLinesFor(preambles, steps, current, t.message)).joinToString("\n")
    , t)

private fun stepLinesFor(preambles: List<Preamble<*>>, steps: List<TestStep<*, *>>, current: Any, errorMessage: String?): List<String> {
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

private fun TestStep<*, *>.toElidedTestNameComponent() = when {
    type == THEN && description.startsWith("And") -> "And…"
    type == THEN -> "Then…"
    else -> description
}

