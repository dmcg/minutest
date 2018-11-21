package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.TestDescriptor
import com.oneeyedmen.minutest.TestTransform
import kotlin.reflect.KType

interface NodeBuilder<F> {
    fun toRuntimeNode(parent: ParentContext<F>): RuntimeNode
}

internal class ContextBuilder<PF, F>(
    private val name: String,
    private val type: KType,
    parentFixtureFactory: ((PF, TestDescriptor) -> F)?,
    private var explicitFixtureFactory: Boolean
) : Context<PF, F>(), NodeBuilder<PF> {

    private val children = mutableListOf<NodeBuilder<F>>()
    private val operations = Operations(parentFixtureFactory)

    override fun privateDeriveFixture(f: (parentFixture: PF, testDescriptor: TestDescriptor) -> F) {
        if (explicitFixtureFactory)
            throw IllegalStateException("Fixture already set in context \"$name\"")
        operations.fixtureFactory = f
        explicitFixtureFactory = true
    }
    
    override fun before(operation: F.() -> Unit) {
        operations.addBefore(operation)
    }

    override fun after(operation: F.() -> Unit) {
        operations.addAfter(operation)
    }

    override fun test_(name: String, f: F.() -> F) {
        children.add(TestBuilder(name, f))
    }

    override fun context(name: String, builder: Context<F, F>.() -> Unit) =
        privateCreateSubContext(name, type, { this }, false, builder)

    override fun <G> privateCreateSubContext(
        name: String,
        type: KType,
        fixtureFactory: (F.(TestDescriptor) -> G)?,
        explicitFixtureFactory: Boolean,
        builder: Context<F, G>.() -> Unit
    ) {
        children.add(ContextBuilder(name, type, fixtureFactory, explicitFixtureFactory).apply(builder))
    }

    override fun addTransform(transform: TestTransform<F>) = operations.addTransform(transform)

    override fun toRuntimeNode(parent: ParentContext<PF>): RuntimeContext<PF, F> {
        operations.tryToResolveFixtureFactory(thereAreTests(), name)
        return RuntimeContext(name, parent, emptyList(), operations).let { context ->
            // nastiness to set up parent child in immutable nodes
            context.copy(children = this.children.map { child -> child.toRuntimeNode(context) })
        }
    }

    private fun thereAreTests() = children.filterIsInstance<TestBuilder<F>>().isEmpty()

}

internal data class TestBuilder<F>(val name: String, val f: F.() -> F) : NodeBuilder<F> {
    override fun toRuntimeNode(parent: ParentContext<F>) = RuntimeTest(name, parent, f)
}