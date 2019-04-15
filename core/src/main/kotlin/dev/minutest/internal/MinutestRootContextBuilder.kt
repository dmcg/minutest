package dev.minutest.internal

import dev.minutest.*
import dev.minutest.experimental.TestAnnotation
import dev.minutest.experimental.prependAnnotations
import dev.minutest.experimental.transformedBy

internal data class MinutestRootContextBuilder<F>(
    private val name: String,
    private val type: FixtureType,
    private val builder: TestContextBuilder<Unit, F>.() -> Unit,
    private val annotations: MutableList<TestAnnotation<Unit>> = mutableListOf(),
    override val transforms: MutableList<NodeTransform<Unit>> = mutableListOf()
) : RootContextBuilder<F> {

    override fun buildNode(): Node<Unit> {
        val rootContext = rootBuilder(name, type, builder).apply {
            // my annotations are those for the root context
            prependAnnotations(annotations)
        }.buildNode()
        val deduplicatedTransformsInTree = rootContext.findRootTransforms().toSet()
        return rootContext.transformedBy(deduplicatedTransformsInTree)
    }

    override fun prependAnnotation(annotation: TestAnnotation<Unit>) {
        annotations.add(0, annotation)
    }
}

private fun <F> rootBuilder(name: String, type: FixtureType, builder: TestContextBuilder<Unit, F>.() -> Unit) =
    MinutestContextBuilder<Unit, F>(name, type, rootFixtureFactoryHack()).apply(builder)

// TODO - this should probably be breadth-first
private fun Node<*>.findRootTransforms(): List<RootTransform> {
    val myTransforms: List<RootTransform> = annotations.mapNotNull { it.rootTransform }
    return when (this) {
        is Test<*> -> myTransforms
        is Context<*, *> -> myTransforms + this.children.flatMap { it.findRootTransforms() }
    }
}