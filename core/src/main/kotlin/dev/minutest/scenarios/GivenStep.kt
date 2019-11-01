package dev.minutest.scenarios

import dev.minutest.TestContextBuilder

internal class GivenStep<F>(
    override val description: String,
    val f: (TestContextBuilder<F, F>).() -> Unit
) : ScenarioStep