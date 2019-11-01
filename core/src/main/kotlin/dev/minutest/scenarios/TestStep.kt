package dev.minutest.scenarios

internal data class TestStep<F, R>(
    override val description: String,
    val type: StepType,
    val f: (F).() -> R
) : ScenarioStep