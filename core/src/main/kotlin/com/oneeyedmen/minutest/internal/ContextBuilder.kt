package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.*

internal class ContextBuilder<PF, F>(
    private val name: String,
    private val type: FixtureType,
    private var fixtureFactory: ((PF, TestDescriptor) -> F)?,
    private var explicitFixtureFactory: Boolean = false
) : Context<PF, F>(), NodeBuilder<PF> {

    private val children = mutableListOf<NodeBuilder<F>>()
    private val befores = mutableListOf<(F) -> Unit>()
    private val afters = mutableListOf<(F) -> Unit>()
    private val transforms = mutableListOf<TestTransform<F>>()

    override fun deriveFixture(f: (PF).() -> F) = deriveInstrumentedFixture { parentFixture, _ ->  parentFixture.f() }

    fun deriveInstrumentedFixture(f: (parentFixture: PF, testDescriptor: TestDescriptor) -> F) {
        if (explicitFixtureFactory)
            throw IllegalStateException("Fixture already set in context \"$name\"")
        fixtureFactory = f
        explicitFixtureFactory = true
    }

    override fun before(operation: F.() -> Unit) {
        befores.add(operation)
    }

    override fun after(operation: F.() -> Unit) {
        afters.add(operation)
    }

    override fun test_(name: String, f: F.() -> F): NodeBuilder<F> =
        TestBuilder(name, f).also { children.add(it) }

    override fun context(name: String, builder: Context<F, F>.() -> Unit) =
        // fixture factory is implicitly identity (return parent fixture (this)
        internalCreateContext(name, type, { this }, builder)

    override fun <G> internalCreateContext(
        name: String,
        type: FixtureType,
        fixtureFactory: (F.(TestDescriptor) -> G)?,
        builder: Context<F, G>.() -> Unit
    ) = ContextBuilder(name, type, fixtureFactory).apply(builder).also { children.add(it) }

    override fun addTransform(transform: TestTransform<F>) {
        transforms.add(transform)
    }

    override fun buildNode(parent: ParentContext<PF>): RuntimeContext = PreparedRuntimeContext(name,
        parent,
        emptyList(),
        befores,
        afters,
        transforms,
        resolvedFixtureFactory(),
        properties).let { context ->
        // nastiness to set up parent child in immutable nodes
        context.copy(children = this.children.map { child -> child.buildNode(context) })
    }

    @Suppress("UNCHECKED_CAST")
    private fun resolvedFixtureFactory(): (PF, TestDescriptor) -> F = when {
        fixtureFactory != null -> fixtureFactory
        thisContextDoesntNeedAFixture() -> { _, _ -> Unit as F }
        // this is safe provided there are only fixture not replaceFixture calls in sub-contexts,
        // as we cannot provide a fixture here to act as receiver. TODO - check somehow
        else -> error("Fixture has not been set in context \"$name\"")
    }!!

    private fun thisContextDoesntNeedAFixture() =
        befores.isEmpty() && afters.isEmpty() && children.filterIsInstance<TestBuilder<F>>().isEmpty()
}