package dev.minutest.internal

import dev.minutest.*
import dev.minutest.experimental.transformedBy

/**
 * A [NodeBuilder] for root contexts that applies the builder only when [buildNode] is called, and also finds and
 * applies [RootTransform]s.
 */
internal data class MinutestRootContextBuilder<F>(
    private val name: String,
    private val type: FixtureType,
    private val builder: TestContextBuilder<Unit, F>.() -> Unit,
    private val markers: MutableList<Any> = mutableListOf(),
    private val transforms: MutableList<NodeTransform<Unit>> = mutableListOf()
) : RootContextBuilder {

    override fun addMarker(marker: Any) {
        markers.add(marker)
    }

    override fun addTransform(transform: NodeTransform<Unit>) {
        transforms.add(transform)
    }

    override fun buildNode(): Node<Unit> {
        val delegate = MinutestContextBuilder<Unit, F>(name, type, rootFixtureFactoryHack(),
            markers = markers.toMutableList(), // [1]
            transforms = transforms.toMutableList() // [1]
            )

        val rootContext = delegate.apply(builder).buildNode()
        val deduplicatedTransformsInTree = rootContext.findRootTransforms().toSet()
        return rootContext.transformedBy(deduplicatedTransformsInTree)
        // [1] - these copies not strictly necessary but they help debugging
    }

    override fun withName(newName: String) = this.copy(name = newName)
}

// TODO - this should probably be breadth-first
private fun Node<*>.findRootTransforms(): List<RootTransform> {
    val myTransforms: List<RootTransform> = markers.filterIsInstance<RootTransform>()
    return when (this) {
        is Test<*> -> myTransforms
        is Context<*, *> -> myTransforms + this.children.flatMap { it.findRootTransforms() }
    }
}