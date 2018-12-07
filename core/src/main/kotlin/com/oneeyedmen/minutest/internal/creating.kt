package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.NodeBuilder
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.TestDescriptor


inline fun <reified F> transformedTopLevelContext(
    name: String,
    noinline transform: (RuntimeNode) -> RuntimeNode,
    noinline builder: Context<Unit, F>.() -> Unit
): NodeBuilder<Unit> =
    TopLevelNodeBuilder(name, askType<F>(), builder, transform)

fun <F> topLevelContext(
    name: String,
    type: FixtureType,
    builder: Context<Unit, F>.() -> Unit
): NodeBuilder<Unit> =
    ContextBuilder<Unit, F>(name, type, fixtureFactoryFor(type))
        .apply(builder)


@Suppress("UNCHECKED_CAST")
private fun <F> fixtureFactoryFor(type: FixtureType): ((Unit, TestDescriptor) -> F)? =
    if (type.classifier == Unit::class) {
        { _, _ -> Unit as F }
    }
    else null

class TopLevelNodeBuilder<F>(
    private val name: String,
    private val type: FixtureType,
    private val builder: Context<Unit, F>.() -> Unit,
    private val transform: (RuntimeNode) -> RuntimeNode
) : NodeBuilder<Unit> {

    override val properties: MutableMap<Any, Any> = HashMap()

    override fun buildNode(parent: ParentContext<Unit>): RuntimeNode {
        return topLevelContext(name, type, builder).buildNode(parent).run(transform)
    }

}