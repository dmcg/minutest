package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.*

class TopLevelContextBuilder<F>(
    private val name: String,
    private val type: FixtureType,
    private val builder: Context<Unit, F>.() -> Unit,
    private val transform: (RuntimeNode) -> RuntimeNode
) : NodeBuilder<Unit, F> {

    override val properties: MutableMap<Any, Any> = HashMap()

    override fun buildNode(): RuntimeContext {
        val topLevelContext = topLevelContext(name, type, builder)
        // we need to apply our annotations to the root, then run the transforms
        topLevelContext.properties.putAll(properties)
        return topLevelContext.buildNode().run(transform) as RuntimeContext
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
