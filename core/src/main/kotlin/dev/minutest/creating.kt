package dev.minutest

import dev.minutest.experimental.TestAnnotation
import dev.minutest.internal.FixtureType
import dev.minutest.internal.MinutestRootContextBuilder
import dev.minutest.internal.askType

/**
 * The entry point to Minutest - defines a context that is not nested within a parent context.
 */
inline fun <reified F> rootContext(noinline builder: TestContextBuilder<Unit, F>.() -> Unit)
    : RootContextBuilder<F> = rootContextBuilder("root", askType<F>(), builder, { it })

/**
 * The entry point to Minutest - defines a named context that is not nested within a parent context.
 */
inline fun <reified F> rootContext(
    name: String,
    noinline builder: TestContextBuilder<Unit, F>.() -> Unit
): RootContextBuilder<F> = rootContextBuilder(name, askType<F>(), builder, { it })

/**
 * The entry point to Minutest - defines a transformed context that is not nested within a parent context.
 */
inline fun <reified F> rootContext(
    noinline transform: (Node<Unit>) -> Node<Unit>,
    noinline builder: TestContextBuilder<Unit, F>.() -> Unit
): RootContextBuilder<F> = rootContextBuilder("root", askType<F>(), builder, transform)

/**
 * The entry point to Minutest - defines a named and transformed context that is not nested within a parent context.
 */
inline fun <reified F> rootContext(
    name: String,
    noinline transform: (Node<Unit>) -> Node<Unit>,
    noinline builder: TestContextBuilder<Unit, F>.() -> Unit
): RootContextBuilder<F> = rootContextBuilder(name, askType<F>(), builder, transform)

@PublishedApi internal fun <F> rootContextBuilder(
    name: String,
    type: FixtureType,
    builder: TestContextBuilder<Unit, F>.() -> Unit,
    transform: (Node<Unit>) -> Node<Unit>,
    annotations: MutableList<TestAnnotation<Unit>> = mutableListOf()
): RootContextBuilder<F> = MinutestRootContextBuilder(name, type, builder, transform, annotations)