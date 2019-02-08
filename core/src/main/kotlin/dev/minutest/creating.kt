package dev.minutest

import dev.minutest.internal.TopLevelContextBuilder
import dev.minutest.internal.askType

/**
 * The entry point to Minutest - defines a context that is not nested within a parent context.
 */
inline fun <reified F> rootContext(
    noinline transform: (dev.minutest.Node<Unit>) -> dev.minutest.Node<Unit> = { it },
    name: String = "root",
    noinline builder: TestContextBuilder<Unit, F>.() -> Unit
) = TopLevelContextBuilder(name, askType<F>(), builder, transform)
