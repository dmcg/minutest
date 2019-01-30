package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.GeneralContextBuilder
import com.oneeyedmen.minutest.NodeBuilder
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.TestDescriptor
import com.oneeyedmen.minutest.experimental.transformedBy

internal class TestContextBuilder<PF, F>(
    private val name: String,
    private val type: FixtureType,
    private var fixtureFactory: ((PF, TestDescriptor) -> F)?,
    private var explicitFixtureFactory: Boolean = false
) : GeneralContextBuilder<PF, F>(), NodeBuilder<PF> {

    private val children = mutableListOf<NodeBuilder<F>>()
    private val befores = mutableListOf<(F, TestDescriptor) -> Unit>()
    private val afters = mutableListOf<(F, TestDescriptor) -> Unit>()
    private var afterAlls = mutableListOf<() -> Unit>()

    override fun deriveFixture(f: (PF).(TestDescriptor) -> F) {
        if (explicitFixtureFactory)
            throw IllegalStateException("Fixture already set in context \"$name\"")
        fixtureFactory = f
        explicitFixtureFactory = true
    }

    override fun before(operation: F.(TestDescriptor) -> Unit) {
        befores.add(operation)
    }

    override fun after(operation: F.(TestDescriptor) -> Unit) {
        afters.add(operation)
    }

    override fun test_(name: String, f: F.(TestDescriptor) -> F): NodeBuilder<F> =
        addChild(TestBuilder(name, f))

    override fun context(name: String, builder: GeneralContextBuilder<F, F>.() -> Unit) =
        // fixture factory is implicitly identity (return parent fixture (this)
        internalCreateContext(name, type, { this }, builder)

    override fun <G> internalCreateContext(
        name: String,
        type: FixtureType,
        fixtureFactory: (F.(TestDescriptor) -> G)?,
        builder: GeneralContextBuilder<F, G>.() -> Unit
    ): NodeBuilder<F> = addChild(TestContextBuilder(name, type, fixtureFactory).apply(builder))
    
    fun <T: NodeBuilder<F>> addChild(child: T): T {
        children.add(child)
        return child
    }

    override fun afterAll(f: () -> Unit) {
        afterAlls.add(f)
    }

    override fun buildNode(): RuntimeNode<PF> = PreparedRuntimeContext(
        name,
        children.map { it.buildNode() },
        befores,
        afters,
        afterAlls,
        resolvedFixtureFactory(),
        annotations
    ).transformedBy(annotations)

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
