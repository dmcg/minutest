package dev.minutest

import dev.minutest.internal.*

/**
 * The entry point to Minutest with no fixture type - defines a context that is not nested within a parent context.
 */
@JvmName("rootContextUnit")
fun rootContext(
    name: String = "root",
    autoFixture: Boolean = true,
    builder: TestContextBuilder<Unit, Unit>.() -> Unit
): RootContextBuilder = rootContext<Unit>(name, autoFixture, builder)

/**
 * The entry point to Minutest with a particular fixture type - defines a context that is not nested within a parent context.
 *
 * You must supply a fixture by calling [TestContextBuilder.fixture].
 */
inline fun <reified F> rootContext(
    name: String = "root",
    autoFixture: Boolean = true,
    noinline builder: TestContextBuilder<Unit, F>.() -> Unit
): RootContextBuilder = rootWithoutFixture(name, askType<F>(), autoFixture, builder)

@PublishedApi
internal fun <F> rootWithoutFixture(
    name: String,
    type: FixtureType<F>,
    autoFixture: Boolean,
    builder: TestContextBuilder<Unit, F>.() -> Unit
) = MinutestRootContextBuilder(
    MinutestContextBuilder(
        name = name,
        parentFixtureType = unitFixtureType,
        fixtureType = type,
        fixtureFactory = UnsafeFixtureFactory(unitFixtureType),
        autoFixture = autoFixture
    ),
    builder
)
