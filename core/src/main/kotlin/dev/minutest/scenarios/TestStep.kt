package dev.minutest.scenarios

internal data class TestStep<F, R>(
    val description: String,
    val type: StepType,
    val f: (F).() -> R
) {
    override fun toString() = description
}