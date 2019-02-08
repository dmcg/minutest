package dev.minutest.internal

import dev.minutest.TestDescriptor
import dev.minutest.experimental.TestAnnotation
import dev.minutest.experimental.TopLevelTransform

data class TopLevelContextBuilder<F>(
    private val name: String,
    private val type: FixtureType,
    private val builder: dev.minutest.TestContextBuilder<Unit, F>.() -> Unit,
    private val transform: (dev.minutest.Node<Unit>) -> dev.minutest.Node<Unit>,
    override val annotations: MutableList<TestAnnotation> = mutableListOf()
) : dev.minutest.NodeBuilder<Unit> {

    override fun buildNode(): dev.minutest.Node<Unit> {
        // we need to apply our annotations to the root, then run the transforms
        val topLevelContext = topLevelContext(name, type, builder).apply {
            annotations.addAll(this@TopLevelContextBuilder.annotations)
        }
        val untransformed = topLevelContext.buildNode()
        val transformsInTree: List<TopLevelTransform> = untransformed.findTopLevelTransforms()
        val allTransforms: List<TopLevelTransform> = transformsInTree + transform.asTopLevelTransform()
        val deduplicatedTransforms = LinkedHashSet(allTransforms)
        val transform = deduplicatedTransforms.reduce{ a, b -> a.then(b) }
        return transform.applyTo(untransformed)
    }
}

private fun ((dev.minutest.Node<Unit>) -> dev.minutest.Node<Unit>).asTopLevelTransform() =
    object : TopLevelTransform {
        override fun applyTo(node: dev.minutest.Node<Unit>): dev.minutest.Node<Unit> =
            this@asTopLevelTransform(node)
    }

private fun <F> topLevelContext(
    name: String,
    type: FixtureType,
    builder: dev.minutest.TestContextBuilder<Unit, F>.() -> Unit
) = MinutestContextBuilder<Unit, F>(name, type, fixtureFactoryFor(type)).apply(builder)

@Suppress("UNCHECKED_CAST")
private fun <F> fixtureFactoryFor(type: FixtureType): ((Unit, TestDescriptor) -> F)? =
    if (type.classifier == Unit::class) {
        { _, _ -> Unit as F }
    } else null

// TODO - this should probably be breadth-first
private fun dev.minutest.Node<*>.findTopLevelTransforms(): List<TopLevelTransform> {
    val myTransforms: List<TopLevelTransform> = annotations.filterIsInstance<TopLevelTransform>()
    return when (this) {
        is dev.minutest.Test<*> -> myTransforms
        is dev.minutest.Context<*, *> -> myTransforms + this.children.flatMap { it.findTopLevelTransforms() }
    }
}