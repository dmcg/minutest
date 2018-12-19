package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.*


inline fun <reified F> transformedTopLevelContext(
    name: String,
    noinline transform: (RuntimeNode) -> RuntimeNode,
    noinline builder: Context<Unit, F>.() -> Unit
): TopLevelContextBuilder<F> = topLevelContextBuilder(name, askType<F>(), builder, transform)

/**
 * An object to hang top-level annotations onto.
 * It passes them on to the top level context and applies transforms to give the root node.
 */
fun <F> topLevelContextBuilder(
    name: String,
    type: FixtureType,
    builder: Context<Unit, F>.() -> Unit,
    transform: (RuntimeNode) -> RuntimeNode
) = object : TopLevelContextBuilder<F> {

    override val properties: MutableMap<Any, Any> = HashMap()

    override fun buildNode(parent: ParentContext<Unit>): RuntimeNode {
        val topLevelContext = topLevelContext(name, type, builder)
        // we need to apply our annotations to the root, then run the transforms
        topLevelContext.properties.putAll(properties)
        return topLevelContext.buildNode(parent).run(transform)
    }
}

private fun <F> topLevelContext(
    name: String,
    type: FixtureType,
    builder: Context<Unit, F>.() -> Unit
): NodeBuilder<Unit, F> = ContextBuilder<Unit, F>(name, type, fixtureFactoryFor(type)).apply(builder)

@Suppress("UNCHECKED_CAST")
private fun <F> fixtureFactoryFor(type: FixtureType): ((Unit, TestDescriptor) -> F)? =
    if (type.classifier == Unit::class) {
        { _, _ -> Unit as F }
    }
    else null
