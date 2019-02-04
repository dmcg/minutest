package uk.org.minutest.internal

import uk.org.minutest.TestDescriptor
import uk.org.minutest.experimental.TestAnnotation
import uk.org.minutest.experimental.TopLevelTransform

data class TopLevelContextBuilder<F>(
    private val name: String,
    private val type: FixtureType,
    private val builder: uk.org.minutest.TestContextBuilder<Unit, F>.() -> Unit,
    private val transform: (uk.org.minutest.Node<Unit>) -> uk.org.minutest.Node<Unit>,
    override val annotations: MutableList<TestAnnotation> = mutableListOf()
) : uk.org.minutest.NodeBuilder<Unit> {

    override fun buildNode(): uk.org.minutest.Node<Unit> {
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

private fun ((uk.org.minutest.Node<Unit>) -> uk.org.minutest.Node<Unit>).asTopLevelTransform() =
    object : TopLevelTransform {
        override fun applyTo(node: uk.org.minutest.Node<Unit>): uk.org.minutest.Node<Unit> =
            this@asTopLevelTransform(node)
    }

private fun <F> topLevelContext(
    name: String,
    type: FixtureType,
    builder: uk.org.minutest.TestContextBuilder<Unit, F>.() -> Unit
) = MinutestContextBuilder<Unit, F>(name, type, fixtureFactoryFor(type)).apply(builder)

@Suppress("UNCHECKED_CAST")
private fun <F> fixtureFactoryFor(type: FixtureType): ((Unit, TestDescriptor) -> F)? =
    if (type.classifier == Unit::class) {
        { _, _ -> Unit as F }
    } else null

// TODO - this should probably be breadth-first
private fun uk.org.minutest.Node<*>.findTopLevelTransforms(): List<TopLevelTransform> {
    val myTransforms: List<TopLevelTransform> = annotations.filterIsInstance<TopLevelTransform>()
    return when (this) {
        is uk.org.minutest.Test<*> -> myTransforms
        is uk.org.minutest.Context<*, *> -> myTransforms + this.children.flatMap { it.findTopLevelTransforms() }
    }
}