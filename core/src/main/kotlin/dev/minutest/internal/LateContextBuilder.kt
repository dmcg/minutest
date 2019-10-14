package dev.minutest.internal

import dev.minutest.Node
import dev.minutest.NodeBuilder
import dev.minutest.TestContextBuilder

/**
 * A [NodeBuilder] that captures markers and transforms applied by prefix
 * [dev.minutest.experimental.TestAnnotation]s.
 */
internal open class LateContextBuilder<PF, F>(
    internal val delegate: MinutestContextBuilder<PF, F>,
    internal val block: TestContextBuilder<PF, F>.() -> Unit
) : NodeBuilder<PF> by delegate {

    constructor(
        name: String,
        parentFixtureType: FixtureType,
        fixtureType: FixtureType,
        fixtureFactory: FixtureFactory<PF, F>,
        autoFixture: Boolean,
        builder: TestContextBuilder<PF, F>.() -> Unit
    ) : this(MinutestContextBuilder(name, parentFixtureType, fixtureType, fixtureFactory, autoFixture), builder)

    override fun buildNode(): Node<PF> = delegate.apply(block).buildNode()
}