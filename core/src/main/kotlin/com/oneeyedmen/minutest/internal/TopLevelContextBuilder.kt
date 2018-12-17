package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.NodeBuilder
import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.TestDescriptor


/**
 * An object to hang top-level annotations onto.
 * It passes them on to a new top level context and applies transforms to give the root node.
 */
data class TopLevelContextBuilder<F>(
    val name: String,
    val type: FixtureType,
    val builder: Context<Unit, F>.() -> Unit,
    val transform: (RuntimeContext<F>) -> RuntimeContext<F>,
    override val properties: MutableMap<Any, Any> = mutableMapOf()
) : NodeBuilder<Unit, F> {

    companion object {
        inline operator fun <reified F> invoke(
            name: String,
            noinline transform: (RuntimeContext<F>) -> RuntimeContext<F>,
            noinline builder: Context<Unit, F>.() -> Unit
        ) = TopLevelContextBuilder(name, askType<F>(), builder, transform)
    }

    override fun buildNode(parent: RuntimeContext<Unit>?): RuntimeContext<F> {
        val delegateBuilder = ContextBuilder<Unit, F>(name, type, fixtureFactoryFor(type)).apply {
            this.properties.putAll(this@TopLevelContextBuilder.properties)
            this.builder()
        }
        return delegateBuilder.buildNode(parent).run(transform)
    }

    fun buildRootNode(): RuntimeContext<F> = buildNode(null)

    @Suppress("UNCHECKED_CAST")
    private fun <F> fixtureFactoryFor(type: FixtureType): ((Unit, TestDescriptor) -> F)? =
        if (type.classifier == Unit::class) {
            { _, _ -> Unit as F }
        }
        else null
}

