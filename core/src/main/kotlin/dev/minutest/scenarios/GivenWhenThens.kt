@file:Suppress("FunctionName")

package dev.minutest.scenarios

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
            TestStep<F, Unit>("$prefix $description", StepType.THEN) {
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
            TestStep<F, R2>("And $description", StepType.WHEN) {
                f().also {
                    next.result = it
                }
            }
        )
        return next
    }

    fun Then(description: String, prefix: String = "Then", f: F.(previousResult: R) -> Unit): ResultingThens<F, R> {
        scenarioBuilder.addStep(
            TestStep<F, Unit>("$prefix $description", StepType.THEN) {
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
            TestStep<F, Unit>("$prefix $description", StepType.THEN) {
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
            TestStep<F, Unit>("$prefix $description", StepType.THEN) {
                this.f()
            }
        )
        return this
    }

    fun And(description: String, f: F.() -> Unit): Thens<F> = Then(description, "And", f)
}