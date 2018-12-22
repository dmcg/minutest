package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.NodeBuilder
import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.TestDescriptor

class TopLevelContextBuilder<F>(
    private val name: String,
    private val type: FixtureType,
    private val builder: Context<Unit, F>.() -> Unit,
    private val transform: (RuntimeContext<Unit, F>) -> RuntimeContext<Unit, F>
) : NodeBuilder<Unit, F> {

    override val properties: MutableMap<Any, Any> = HashMap()

    override fun buildNode(): RuntimeContext<Unit, F> {
        // we need to apply our annotations to the root, then run the transforms
        val topLevelContext = topLevelContext(name, type, builder).apply {
            properties.putAll(this@TopLevelContextBuilder.properties)
        }
        return topLevelContext.buildNode().run(transform)
    }
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
    }
    else null
