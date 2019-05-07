package dev.minutest.internal

import dev.minutest.*
import dev.minutest.experimental.transformedBy

/**
 * A [LateContextBuilder] for root contexts that finds and applies [RootTransform]s.
 */
internal class MinutestRootContextBuilder<F>(
    delegate: MinutestContextBuilder<Unit, F>,
    builder: TestContextBuilder<Unit, F>.() -> Unit
) : LateContextBuilder<Unit, F>(delegate, builder), RootContextBuilder {

    override fun buildNode(): Node<Unit> {
        val rootContext = super.buildNode()
        val deduplicatedTransformsInTree = rootContext.findRootTransforms().toSet()
        return rootContext.transformedBy(deduplicatedTransformsInTree)
    }

    override fun withName(newName: String) = MinutestRootContextBuilder(
        delegate.copy(name = newName),
        builder
    )
}

// TODO - this should probably be breadth-first
private fun Node<*>.findRootTransforms(): List<RootTransform> {
    val myTransforms: List<RootTransform> = markers.filterIsInstance<RootTransform>()
    return when (this) {
        is Test<*> -> myTransforms
        is Context<*, *> -> myTransforms + this.children.flatMap { it.findRootTransforms() }
    }
}