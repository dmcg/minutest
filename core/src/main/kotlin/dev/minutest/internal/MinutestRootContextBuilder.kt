package dev.minutest.internal

import dev.minutest.*
import dev.minutest.experimental.transformedBy

/**
 * A [TestContextBuilder] for root contexts that finds and applies [RootTransform]s.
 */
internal data class MinutestRootContextBuilder<F>(
    private val delegate: MinutestContextBuilder<Unit, F>
) : RootContextBuilder, NodeBuilder<Unit> by delegate {

    constructor(
        name: String? = null,
        type: FixtureType,
        block: TestContextBuilder<Unit, F>.() -> Unit
    ) : this(
        MinutestContextBuilder(
            name ?: defaultRootName,
            unitFixtureType,
            type,
            UnsafeFixtureFactory(unitFixtureType),
            block = block
        )
    )

    override fun buildNode(): Node<Unit> {
        val rootContext = delegate.buildNode()
        val deduplicatedTransformsInTree = rootContext.findRootTransforms().toSet()
        return rootContext.transformedBy(deduplicatedTransformsInTree)
    }

    override fun withNameUnlessSpecified(newName: String) =
        when (delegate.name) {
            defaultRootName -> MinutestRootContextBuilder(
                delegate.copy(name = newName)
            )
            else -> this
        }
}

// TODO - this should probably be breadth-first
internal fun Node<*>.findRootTransforms(): List<RootTransform> {
    val myTransforms: List<RootTransform> = markers.filterIsInstance<RootTransform>()
    return when (this) {
        is Test<*> -> myTransforms
        is Context<*, *> -> myTransforms + this.children.flatMap { it.findRootTransforms() }
    }
}

private val defaultRootName = "root"