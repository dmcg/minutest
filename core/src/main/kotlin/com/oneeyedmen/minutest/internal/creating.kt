package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.*
import com.oneeyedmen.minutest.experimental.TestAnnotation
import com.oneeyedmen.minutest.experimental.TopLevelContextTransform

data class TopLevelContextBuilder<F>(
    private val name: String,
    private val type: FixtureType,
    private val builder: Context<Unit, F>.() -> Unit,
    private val transform: (RuntimeNode<Unit>) -> RuntimeNode<Unit>,
    override val annotations: MutableList<TestAnnotation> = mutableListOf()
) : NodeBuilder<Unit> {

    override fun buildNode(): RuntimeNode<Unit> {
        // we need to apply our annotations to the root, then run the transforms
        val topLevelContext = topLevelContext(name, type, builder).apply {
            annotations.addAll(this@TopLevelContextBuilder.annotations)
        }
        val untransformed = topLevelContext.buildNode()
        val transformsInTree: List<TopLevelContextTransform> = untransformed.findTopLevelTransforms()
        val allTransforms: List<TopLevelContextTransform> = transformsInTree + transform.asTopLevelContextTransform()
        val deduplicatedTransforms = LinkedHashSet(allTransforms)
        val transform = deduplicatedTransforms.reduce{ a, b ->
            (a as TopLevelContextTransform).then(b as TopLevelContextTransform)
        }
        return (transform as TopLevelContextTransform).applyTo(untransformed)
    }
}

private fun ((RuntimeNode<Unit>) -> RuntimeNode<Unit>).asTopLevelContextTransform() =
    object : TopLevelContextTransform {
        override fun applyTo(node: RuntimeNode<Unit>): RuntimeNode<Unit> =
            this@asTopLevelContextTransform(node)
    }

private fun <F> topLevelContext(
    name: String,
    type: FixtureType,
    builder: Context<Unit, F>.() -> Unit
) = ContextBuilder<Unit, F>(name, type, fixtureFactoryFor(type)).apply(builder)

@Suppress("UNCHECKED_CAST")
private fun <F> fixtureFactoryFor(type: FixtureType): ((Unit, TestDescriptor) -> F)? =
    if (type.classifier == Unit::class) {
        { _, _ -> Unit as F }
    } else null

// TODO - this should probably be breadth-first
private fun RuntimeNode<*>.findTopLevelTransforms(): List<TopLevelContextTransform> {
    val myTransforms: List<TopLevelContextTransform> = annotations.filterIsInstance<TopLevelContextTransform>()
    return when (this) {
        is RuntimeTest<*> -> myTransforms
        is RuntimeContext<*, *> -> myTransforms + this.children.flatMap { it.findTopLevelTransforms() }
    }
}