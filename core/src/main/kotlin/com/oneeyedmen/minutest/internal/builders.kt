package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.TestTransform

internal sealed class NodeBuilder<F> {
    abstract fun toNode(parent: ParentContext<F>): Node
}

internal class ContextBuilder<PF, F>(
    private val name: String,
    private var fixtureFn: (PF.() -> F)? = null
) : Context<PF, F>, NodeBuilder<PF>() {

    private var fixtureCalled = false
    private val children = mutableListOf<NodeBuilder<F>>()
    private val operations = Operations<F>()

    override fun fixture(factory: PF.() -> F) {
        if (fixtureCalled)
            throw IllegalStateException("Fixture already set in context \"$name\"")
        fixtureFn = factory
        fixtureCalled = true
    }

    override fun before(operation: F.() -> Unit) {
        operations.befores.add(operation)
    }

    override fun after(operation: F.() -> Unit) {
        operations.afters.add(operation)
    }

    override fun test_(name: String, f: F.() -> F) {
        TestBuilder(name, f).also { children.add(it) }
    }

    override fun test(name: String, f: F.() -> Unit) = test_(name) { this.apply(f) }

    /**
     * Define a sub-context, inheriting the fixture from this.
     */
    override fun context(name: String, builder: Context<F, F>.() -> Unit) {
        createSubContext(name, { this }, builder)
    }

    override fun <G> derivedContext(name: String, builder: Context<F, G>.() -> Unit) {
        createSubContext(name, null, builder)
    }

    private fun <G> createSubContext(name: String, fixtureFn: (F.() -> G)?, builder: Context<F, G>.() -> Unit) {
        ContextBuilder(name, fixtureFn).also {
            it.builder()
            children.add(it)
        }
    }

    override fun addTransform(transform: TestTransform<F>) {
        operations.transforms += transform
    }

    override fun toNode(parent: ParentContext<PF>): MiContext<PF, F> {
        val fixtureFactory = fixtureFn ?: error("Fixture has not been set in context \"$name\"")
        return MiContext(name, parent, fixtureFactory, emptyList(), operations).let { context ->
            // nastiness to set up parent child in immutable nodes
            context.copy(children = this.children.map { child -> child.toNode(context) })
        }
    }
}

internal data class TestBuilder<F>(val name: String, val f: F.() -> F) : NodeBuilder<F>() {
    override fun toNode(parent: ParentContext<F>) = MinuTest(name, parent, f)
}