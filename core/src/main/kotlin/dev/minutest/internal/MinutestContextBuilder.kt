package dev.minutest.internal

import dev.minutest.Node
import dev.minutest.NodeBuilder
import dev.minutest.TestContextBuilder
import dev.minutest.TestDescriptor
import dev.minutest.experimental.TestAnnotation
import dev.minutest.experimental.transformedBy

/**
 * Internal implementation of [TestContextBuilder] which hides the details and the [NodeBuilder]ness.
 */
internal class MinutestContextBuilder<PF, F>(
    private val name: String,
    private val type: FixtureType,
    private var fixtureFactory: FixtureFactory<PF, F>?
) : TestContextBuilder<PF, F>(), NodeBuilder<PF> {

    private var explicitFixtureFactory = false
    private val children = mutableListOf<NodeBuilder<F>>()
    private val befores = mutableListOf<(F, TestDescriptor) -> Unit>()
    private val afters = mutableListOf<(F, TestDescriptor) -> Unit>()
    private val afterAlls = mutableListOf<() -> Unit>()

    override val annotations: MutableList<TestAnnotation> = mutableListOf()

    override fun deriveFixture(f: (PF).(TestDescriptor) -> F) {
        if (explicitFixtureFactory)
            throw IllegalStateException("Fixture already set in context \"$name\"")
        fixtureFactory = FixtureFactory(type, f)
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

    override fun context(name: String, builder: TestContextBuilder<F, F>.() -> Unit) =
        newContext(
            name = name,
            type = type,
            fixtureFactory = { f, _ -> f }, // sub-context fixtureFactory defaults to the fixture of the parent
            builder = builder)

    override fun <G> internalDerivedContext(
        name: String,
        type: FixtureType,
        builder: TestContextBuilder<F, G>.() -> Unit
    ): NodeBuilder<F> = newContext(
        name = name,
        type = type,
        fixtureFactory = null, // subContext can't have parent fixture factory because the types have changed
        builder = builder
    )

    private fun <G> newContext(
        name: String,
        type: FixtureType,
        fixtureFactory: ((F, TestDescriptor) -> G)?,
        builder: TestContextBuilder<F, G>.() -> Unit
    ): NodeBuilder<F> = addChild(
        MinutestContextBuilder(
            name,
            type,
            fixtureFactory?.let { FixtureFactory(type, fixtureFactory) }
        ).apply(builder)
    )
    
    private fun <T: NodeBuilder<F>> addChild(child: T): T {
        children.add(child)
        return child
    }

    override fun afterAll(f: () -> Unit) {
        afterAlls.add(f)
    }

    override fun buildNode(): Node<PF> = PreparedContext(
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
        fixtureFactory != null ->
            fixtureFactory ?: error("concurrent modification of fixture factory")
        thisContextDoesntNeedAFixture() ->
            { _, _ -> Unit as F }
        // this is safe provided there are only fixture not replaceFixture calls in sub-contexts,
        // as we cannot provide a fixture here to act as receiver. TODO - check somehow
        else ->
            error("Fixture has not been set in context \"$name\"")
    }

    private fun thisContextDoesntNeedAFixture() =
        befores.isEmpty() && afters.isEmpty() && children.filterIsInstance<TestBuilder<F>>().isEmpty()
}


