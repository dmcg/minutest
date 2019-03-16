package dev.minutest.internal

import dev.minutest.*
import dev.minutest.experimental.TestAnnotation
import dev.minutest.experimental.prependAnnotations

internal data class MinutestRootContextBuilder<F>(
    private val name: String,
    private val type: FixtureType,
    private val builder: TestContextBuilder<Unit, F>.() -> Unit,
    private val transform: RootTransform,
    private val annotations: MutableList<TestAnnotation<Unit>> = mutableListOf()
) : RootContextBuilder<F> {


    override fun buildNode(): Node<Unit> {
        // we need to apply our annotations to the root, then run the transforms
        val rootBuilder = rootBuilder(name, type, builder).apply {
            prependAnnotations(this@MinutestRootContextBuilder.annotations)
        }
        val untransformed = rootBuilder.buildNode()
        val transformsInTree = untransformed.findRootTransforms()
        val deduplicatedTransforms = (transformsInTree + transform).toSet() // [1]
        val transform = deduplicatedTransforms.reduce { a, b -> a.then(b) }
        return transform.transform(untransformed)
    }

    override fun appendAnnotation(annotation: TestAnnotation<Unit>) {
        annotations.add(annotation)
    }

    override fun prependAnnotation(annotation: TestAnnotation<Unit>) {
        annotations.add(0, annotation)
    }

    // 1 - using the transforms in the tree first keeps tests passing, largely I think because it allows FOCUS to
    // be applied before logging.
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