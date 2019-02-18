package dev.minutest.internal

import dev.minutest.*
import dev.minutest.experimental.TestAnnotation
import dev.minutest.experimental.TopLevelTransform

internal data class MinutestRootContextBuilder<F>(
    private val name: String,
    private val type: FixtureType,
    private val builder: TestContextBuilder<Unit, F>.() -> Unit,
    private val transform: (Node<Unit>) -> Node<Unit>,
    override val annotations: MutableList<TestAnnotation> = mutableListOf()
) : RootContextBuilder<F> {

    override fun buildNode(): Node<Unit> {
        // we need to apply our annotations to the root, then run the transforms
        val rootBuilder = rootBuilder(name, type, builder).apply {
            annotations.addAll(this@MinutestRootContextBuilder.annotations)
        }
        val untransformed = rootBuilder.buildNode()
        val transformsInTree: List<TopLevelTransform> = untransformed.findTopLevelTransforms()
        val allTransforms: List<TopLevelTransform> = transformsInTree + transform.asTopLevelTransform()
        val deduplicatedTransforms = LinkedHashSet(allTransforms)
        val transform = deduplicatedTransforms.reduce { a, b -> a.then(b) }
        return transform.applyTo(untransformed)
    }
}

private fun ((Node<Unit>) -> Node<Unit>).asTopLevelTransform() =
    object : TopLevelTransform {
        override fun applyTo(node: Node<Unit>): Node<Unit> =
            this@asTopLevelTransform(node)
    }

private fun <F> rootBuilder(name: String, type: FixtureType, builder: TestContextBuilder<Unit, F>.() -> Unit) =
    MinutestContextBuilder<Unit, F>(name, type, rootFixtureFactoryHack()).apply(builder)

// TODO - this should probably be breadth-first
private fun Node<*>.findTopLevelTransforms(): List<TopLevelTransform> {
    val myTransforms: List<TopLevelTransform> = annotations.filterIsInstance<TopLevelTransform>()
    return when (this) {
        is Test<*> -> myTransforms
        is Context<*, *> -> myTransforms + this.children.flatMap { it.findTopLevelTransforms() }
    }
}