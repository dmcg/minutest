package dev.minutest

import dev.minutest.internal.*

/**
 * The entry point to Minutest without any fixture - defines a context that is not nested within a parent context.
 */
@JvmName("rootContextUnit")
fun rootContext(
    name: String = "root",
    builder: TestContextBuilder<Unit, Unit>.() -> Unit
): RootContextBuilder = rootContextBuilder(name, askType<Unit>(), builder, rootFixtureFactory)


/**
 * The entry point to Minutest without initial fixture - defines a context that is not nested within a parent context.
 */
inline fun <reified F> rootContext(
    name: String = "root",
    noinline builder: TestContextBuilder<Unit, F>.() -> Unit
): RootContextBuilder = rootWithoutFixture(name, askType<F>(), builder)

@PublishedApi
internal fun <F> rootWithoutFixture(
    name: String,
    type: FixtureType,
    builder: TestContextBuilder<Unit, F>.() -> Unit
) = rootContextBuilder(name, type, builder, rootFixtureFactoryHack())

@PublishedApi
internal fun <F> rootContextBuilder(
    name: String,
    type: FixtureType,
    builder: TestContextBuilder<Unit, F>.() -> Unit,
    fixtureFactory: FixtureFactory<Unit, F>
): RootContextBuilder = MinutestRootContextBuilder(MinutestContextBuilder(name, type, fixtureFactory), builder)
