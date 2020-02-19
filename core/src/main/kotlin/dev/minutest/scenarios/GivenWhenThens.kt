@file:Suppress("FunctionName")

package dev.minutest.scenarios

class Givens<F, R>(
    private val scenarioBuilder: ScenarioBuilder<F>,
    private val resultHolder: ResultHolder<R>
) {

    fun <R2> And(description: String, operation: F.() -> R2): Givens<F, R2> =
        scenarioBuilder.Given(description, "And", operation)

    fun Then(description: String, prefix: String = "Then", f: F.(previousResult: R) -> Unit): ResultingThens<F, R> {
        scenarioBuilder.addStep(
            TestStep<F, Unit>("$prefix $description", StepType.THEN) {
                this.f(resultHolder.value)
            }
        )
        return ResultingThens(scenarioBuilder, resultHolder)
    }
}

class Whens<F, R>(
    private val scenarioBuilder: ScenarioBuilder<F>,
    private val resultHolder: ResultHolder<R>
) {
    fun <R2> And(description: String, f: F.() -> R2): Whens<F, R2> {
        val resultHolder = ResultHolder<R2>()
        scenarioBuilder.addStep(
            TestStep<F, R2>("And $description", StepType.WHEN) {
                f().also {
                    resultHolder.value = it
                }
            }
        )
        return Whens(scenarioBuilder, resultHolder)
    }

    fun Then(description: String, prefix: String = "Then", f: F.(previousResult: R) -> Unit): ResultingThens<F, R> {
        scenarioBuilder.addStep(
            TestStep<F, Unit>("$prefix $description", StepType.THEN) {
                this.f(resultHolder.value)
            }
        )
        return ResultingThens(scenarioBuilder, resultHolder)
    }
}

class ResultingThens<F, R>(
    private val scenarioBuilder: ScenarioBuilder<F>,
    private val resultHolder: ResultHolder<R>
) {
    fun Then(description: String, prefix: String = "Then", f: F.(previousResult: R) -> Unit): ResultingThens<F, R> {
        scenarioBuilder.addStep(
            TestStep<F, Unit>("$prefix $description", StepType.THEN) {
                this.f(resultHolder.value)
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
            TestStep<F, Unit>("$prefix $description", StepType.THEN) {
                this.f()
            }
        )
        return this
    }

    fun And(description: String, f: F.() -> Unit): Thens<F> = Then(description, "And", f)
}