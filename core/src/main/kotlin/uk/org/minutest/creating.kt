package uk.org.minutest

import uk.org.minutest.internal.TopLevelContextBuilder
import uk.org.minutest.internal.askType

/**
 * The entry point to Minutest - defines a context that is not nested within a parent context.
 */
inline fun <reified F> rootContext(
    noinline transform: (uk.org.minutest.Node<Unit>) -> uk.org.minutest.Node<Unit> = { it },
    name: String = "root",
    noinline builder: TestContextBuilder<Unit, F>.() -> Unit
) = TopLevelContextBuilder(name, askType<F>(), builder, transform)
