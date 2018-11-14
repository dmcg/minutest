package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.Focusable
import com.oneeyedmen.minutest.TestDescriptor
import com.oneeyedmen.minutest.TestTransform
import kotlin.reflect.KType

internal interface NodeBuilder<F> : Focusable {
    fun toTestNode(parent: ParentContext<F>): TestNode
}

internal class ContextBuilder<PF, F>(
    private val name: String,
    private val type: KType,
    parentFixtureFactory: ((PF, TestDescriptor) -> F)?,
    private var explicitFixtureFactory: Boolean
) : Context<PF, F>(), NodeBuilder<PF> {

    private val children = mutableListOf<NodeBuilder<F>>()
    private val operations = Operations(parentFixtureFactory)
    var hasFocused: Boolean = false
    override var isFocused: Boolean = false

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

    override fun test_(name: String, f: F.() -> F): Focusable = TestBuilder(name, f).also {
        children.add(it)
    }

    override fun context(name: String, builder: Context<F, F>.() -> Unit) =
        privateCreateSubContext(name, type, { this }, false, builder)

    override fun <G> privateCreateSubContext(
        name: String,
        type: KType,
        fixtureFactory: (F.(TestDescriptor) -> G)?,
        explicitFixtureFactory: Boolean,
        builder: Context<F, G>.() -> Unit
    ): Focusable =
        ContextBuilder(name, type, fixtureFactory, explicitFixtureFactory).apply(builder).also {
            children.add(it)
        }


    override fun addTransform(transform: TestTransform<F>) = operations.addTransform(transform)

    override fun toTestNode(parent: ParentContext<PF>): RuntimeContext<PF, F> {
        operations.tryToResolveFixtureFactory(thereAreTests(), name)
        return RuntimeContext(name, parent, emptyList(), operations).let { context ->
            // nastiness to set up parent child in immutable nodes
            val relevantChildren = if (hasFocused) children.filter { it.isFocused  } else children
            context.copy(children = relevantChildren.map { child -> child.toTestNode(context) })
        }
    }

    private fun thereAreTests() = children.filterIsInstance<TestBuilder<F>>().isEmpty()

}

internal data class TestBuilder<F>(val name: String, val f: F.() -> F) : NodeBuilder<F>, Focusable {

    override fun toTestNode(parent: ParentContext<F>) = RuntimeTest(name, parent, f)

    override var isFocused: Boolean = false
}