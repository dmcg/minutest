package dev.minutest.internal

import dev.minutest.*
import dev.minutest.experimental.transformedBy

/**
 * A [NodeBuilder] for root contexts that applies the builder only when [buildNode] is called, and also finds and
 * applies [RootTransform]s.
 */
internal data class MinutestRootContextBuilder<F>(
    private val delegate: LateContextBuilder<Unit, F>
) : NodeBuilder<Unit> by delegate, RootContextBuilder {

    constructor(
        name: String,
        type: FixtureType,
        builder: TestContextBuilder<Unit, F>.() -> Unit
    ) : this(LateContextBuilder<Unit, F>(name, type, rootFixtureFactoryHack(), builder))

    override fun buildNode(): Node<Unit> {
        val rootContext = delegate.buildNode()
        val deduplicatedTransformsInTree = rootContext.findRootTransforms().toSet()
        return rootContext.transformedBy(deduplicatedTransformsInTree)
    }

    override fun withName(newName: String) = copy(delegate = delegate.copy(name = newName))
}

// TODO - this should probably be breadth-first
private fun Node<*>.findRootTransforms(): List<RootTransform> {
    val myTransforms: List<RootTransform> = markers.filterIsInstance<RootTransform>()
    return when (this) {
        is Test<*> -> myTransforms
        is Context<*, *> -> myTransforms + this.children.flatMap { it.findRootTransforms() }
    }
}