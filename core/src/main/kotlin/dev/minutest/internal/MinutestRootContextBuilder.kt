package dev.minutest.internal

import dev.minutest.*
import dev.minutest.experimental.TestAnnotation
import dev.minutest.experimental.transformedBy

/**
 * A [NodeBuilder] for root contexts that applies the builder only when [buildNode] is called, and also finds and
 * applies [RootTransform]s.
 */
internal data class MinutestRootContextBuilder<F>(
    private val delegate: MinutestContextBuilder<Unit, F>,
    private val builder: TestContextBuilder<Unit, F>.() -> Unit
) : RootContextBuilder, NodeBuilder<Unit> by delegate {

    constructor(
        name: String,
        type: FixtureType,
        builder: TestContextBuilder<Unit, F>.() -> Unit
    ) : this(MinutestContextBuilder<Unit, F>(name, type, rootFixtureFactoryHack()), builder)


    override fun buildNode(): Node<Unit> {
        val rootContext = delegate.apply(builder).buildNode()
        val deduplicatedTransformsInTree = rootContext.findRootTransforms().toSet()
        return rootContext.transformedBy(deduplicatedTransformsInTree)
    }

    override fun withName(newName: String) = this.copy(delegate = delegate.copy(name = newName))
}

// TODO - this should probably be breadth-first
private fun Node<*>.findRootTransforms(): List<RootTransform> {
    val myTransforms: List<RootTransform> = annotations.filterIsInstance<TestAnnotation<*>>().mapNotNull { it.rootTransform }
    return when (this) {
        is Test<*> -> myTransforms
        is Context<*, *> -> myTransforms + this.children.flatMap { it.findRootTransforms() }
    }
}