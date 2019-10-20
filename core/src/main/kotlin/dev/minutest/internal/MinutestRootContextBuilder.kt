package dev.minutest.internal

import dev.minutest.*
import dev.minutest.experimental.transformedBy

/**
 * A [TestContextBuilder] for root contexts that finds and applies [RootTransform]s.
 */
internal data class MinutestRootContextBuilder<F>(
    private val delegate: MinutestContextBuilder<Unit, F>
) : RootContextBuilder, NodeBuilder<Unit> by delegate {

    constructor(name: String, type: FixtureType, block: TestContextBuilder<Unit, F>.() -> Unit) : this(
        MinutestContextBuilder(name, unitFixtureType, type, UnsafeFixtureFactory(unitFixtureType), block = block)
    )

    override fun buildNode(): Node<Unit> {
        val rootContext = delegate.buildNode()
        val deduplicatedTransformsInTree = rootContext.findRootTransforms().toSet()
        return rootContext.transformedBy(deduplicatedTransformsInTree)
    }

    override fun withName(newName: String) = MinutestRootContextBuilder(
        delegate.copy(name = newName)
    )
}

// TODO - this should probably be breadth-first
internal fun Node<*>.findRootTransforms(): List<RootTransform> {
    val myTransforms: List<RootTransform> = markers.filterIsInstance<RootTransform>()
    return when (this) {
        is Test<*> -> myTransforms
        is Context<*, *> -> myTransforms + this.children.flatMap { it.findRootTransforms() }
    }
}