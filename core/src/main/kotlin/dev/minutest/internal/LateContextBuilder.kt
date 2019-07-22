package dev.minutest.internal

import dev.minutest.Node
import dev.minutest.NodeBuilder
import dev.minutest.TestContextBuilder

/**
 * A [NodeBuilder] that captures markers and transforms applied by prefix
 * [dev.minutest.experimental.TestAnnotation]s.
 */
internal open class LateContextBuilder<PF, F>(
    protected val delegate: MinutestContextBuilder<PF, F>,
    protected val block: TestContextBuilder<PF, F>.() -> Unit
) : NodeBuilder<PF> by delegate {

    constructor(
        name: String,
        parentFixtureType: FixtureType,
        fixtureType: FixtureType,
        fixtureFactory: FixtureFactory<PF, F>,
        builder: TestContextBuilder<PF, F>.() -> Unit
    ) : this(MinutestContextBuilder(name, parentFixtureType, fixtureType, fixtureFactory), builder)

    override fun buildNode(): Node<PF> = delegate.apply(block).buildNode()
}