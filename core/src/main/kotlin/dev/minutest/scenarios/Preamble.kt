package dev.minutest.scenarios

import dev.minutest.TestContextBuilder

internal class Preamble<F>(
    val description: String,
    val f: (TestContextBuilder<F, F>).() -> Unit
) {
    override fun toString() = description
}