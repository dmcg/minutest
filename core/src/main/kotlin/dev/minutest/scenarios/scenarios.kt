@file:Suppress("FunctionName")

package dev.minutest.scenarios

import dev.minutest.*
import dev.minutest.scenarios.StepType.*

fun <F> ContextBuilder<F>.Scenario(
    description: String? = null,
    elideTestNameAfterLength: Int = 128,
    block: ScenarioBuilder<F>.() -> Unit
) =
    ScenarioBuilder<F>(description, elideTestNameAfterLength).apply(block).applyToContext(this)

@MinutestFixture
class ScenarioBuilder<F>(
    private val description: String?,
    private var elideTestNameAfterLength: Int
) {
    private val givenSteps: MutableList<GivenStep<F>> = mutableListOf()
    private val testSteps: MutableList<TestStep<F, *>> = mutableListOf()

    fun GivenFixture(description: String, factory: (Unit).() -> F): Givens<F, F> {
        val nextResult = ResultHolder<F>()
        lateinit var cantAccessThisInCtor: GivenStep<F>
        givenSteps.add(
            GivenStep<F>("Given $description") {
                given {
                    this@ScenarioBuilder.tryThrowingScenarioFailedException(cantAccessThisInCtor) {
                        factory(Unit).also { nextResult.value = it }
                    }
                }
            }.also { cantAccessThisInCtor = it }
        )
        return Givens(this, nextResult)
    }

    fun <R> Given(description: String, prefix: String = "Given", operation: F.() -> R): Givens<F, R> {
        val nextResult = ResultHolder<R>()
        lateinit var cantAccessThisInCtor: GivenStep<F>
        givenSteps.add(
            GivenStep<F>("$prefix $description") {
                beforeEach {
                    this@ScenarioBuilder.tryThrowingScenarioFailedException(cantAccessThisInCtor) {
                        operation().also { nextResult.value = it }
                    }
                }
            }.also { cantAccessThisInCtor = it }
        )
        return Givens(this, nextResult)
    }

    fun <R> When(description: String, f: F.() -> R): Whens<F, R> {
        val nextResult = ResultHolder<R>()
        addStep(
            TestStep<F, R>("When $description", WHEN) {
                f().also {
                    nextResult.value = it
                }
            }
        )
        return Whens(this, nextResult)
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
        testSteps.add(
            if (step.type == INFER)
                step.copy(type = testSteps.lastOrNull()?.type ?: WHEN)
            else
                step
        )
    }

    internal fun applyToContext(contextBuilder: ContextBuilder<F>) {
        val scenarioBuilder = this@ScenarioBuilder
        val topLevelName = description ?: generateName()
        if (givenSteps.isNotEmpty()) {
            contextBuilder.context(topLevelName) {
                val newContext = this
                scenarioBuilder.givenSteps.forEach { preamble ->
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
        contextBuilder.test2(testName) {
            testSteps.forEach { step ->
                tryThrowingScenarioFailedException(step) {
                    step.f(this)
                }
            }
        }
    }

    private fun <R> tryThrowingScenarioFailedException(step: ScenarioStep, f: () -> R): R =
        try {
            f()
        } catch (t: Throwable) {
            throw scenarioFailedExceptionFor(description ?: generateName(), givenSteps + testSteps, step, t)
        }

    private fun generateName(): String {
        val candidate = (givenSteps.map { it.description } + testSteps.map { it.description }).joinToString()
        return when {
            candidate.length <= elideTestNameAfterLength -> candidate
            else -> (givenSteps.map { it.description } + testSteps.map { it.toElidedTestNameComponent() }).joinToString()
        }
    }
}

private fun scenarioFailedExceptionFor(
    scenarioDescription: String, steps: List<ScenarioStep>, failingStep: ScenarioStep, t: Throwable
) =
    ScenarioStepFailedException(
        (listOf("", "Error in $scenarioDescription") + stepLinesFor(steps, failingStep, t.message)).joinToString("\n"),
        t
    )

private fun stepLinesFor(steps: List<ScenarioStep>, failingStep: ScenarioStep, errorMessage: String?): List<String> {
    val failingIndex = steps.indexOf(failingStep)
    val beforeLines = steps.subList(0, failingIndex).map { "✓ ${it.description}" }
    val failingLine = "X " + steps[failingIndex].description
    val afterLines = steps.subList(failingIndex + 1, steps.size).map { "- ${it.description}" }
    return (beforeLines + failingLine + errorMessage + afterLines).filterNotNull()
}

private fun TestStep<*, *>.toElidedTestNameComponent() = when {
    type == THEN && description.startsWith("And") -> "And…"
    type == THEN -> "Then…"
    else -> description
}

