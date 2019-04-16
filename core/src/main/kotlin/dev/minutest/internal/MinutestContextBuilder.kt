package dev.minutest.internal

import dev.minutest.*
import dev.minutest.experimental.TestAnnotation
import dev.minutest.experimental.transformedBy

/**
 * Internal implementation of [TestContextBuilder] which hides the details and the [NodeBuilder]ness.
 */
internal data class MinutestContextBuilder<PF, F>(
    val name: String,
    private val type: FixtureType,
    private var fixtureFactory: FixtureFactory<PF, F>,
    private var explicitFixtureFactory: Boolean = false,
    private val children: MutableList<NodeBuilder<F>> = mutableListOf(),
    private val befores: MutableList<(F, TestDescriptor) -> F> = mutableListOf(),
    private val afters: MutableList<(FixtureValue<F>, TestDescriptor) -> Unit> = mutableListOf(),
    private val afterAlls: MutableList<() -> Unit> = mutableListOf(),
    private val annotations: MutableList<TestAnnotation<PF>> = mutableListOf(),
    override val transforms: MutableList<NodeTransform<PF>> = mutableListOf()
) : TestContextBuilder<PF, F>(), NodeBuilder<PF> {



    override fun deriveFixture(f: (PF).(TestDescriptor) -> F) {
        if (explicitFixtureFactory)
            throw IllegalStateException("Fixture already set in context \"$name\"")
        fixtureFactory = FixtureFactory(type, f)
        explicitFixtureFactory = true
    }

    override fun before(operation: F.(TestDescriptor) -> Unit) {
        before_ { testDescriptor ->
            this.operation(testDescriptor)
            this
        }
    }

    override fun before_(f: F.(TestDescriptor) -> F) {
        befores.add(f)
    }

    override fun after(operation: F.(TestDescriptor) -> Unit) {
        afters.add { result, testDescriptor -> result.value.operation(testDescriptor) }
    }

    override fun after2(operation: FixtureValue<F>.(TestDescriptor) -> Unit) {
        afters.add(operation)
    }

    override fun test_(name: String, f: F.(TestDescriptor) -> F): NodeBuilder<F> =
        addChild(TestBuilder(name, f))

    override fun context(name: String, builder: TestContextBuilder<F, F>.() -> Unit) =
        newContext(
            name,
            type,
            FixtureFactory(fixtureFactory.type) { f, _ -> f }, // [1]
            builder)
    /* 1 - We don't know for sure that the type of our fixtureFactory is the same as our type, so we pass it on
       so that checkedFixtureFactory() can do the right thing.

       The value returned by the factory in our child should be the value that this context has computed -
       that's the f in the block
     */

    override fun <G> internalDerivedContext(
        name: String,
        type: FixtureType,
        builder: TestContextBuilder<F, G>.() -> Unit
    ): NodeBuilder<F> = newContext(
        name,
        type,
        FixtureFactory(this.type) { _, _ -> error("Please report sighting of wrong fixture factory") }, // [2]
        builder
    )
    /* 2 - If you're deriving a context we know that we don't know how to build a fixture any more. So we pass on
       a FixtureBuilder with the parent type so that checkedFixtureFactory() can reject it, and error if it doesn't.
     */

    override fun addAnnotation(annotation: TestAnnotation<PF>) {
        annotations.add(annotation)
    }

    private fun <G> newContext(
        name: String,
        type: FixtureType,
        fixtureFactory: FixtureFactory<F, G>,
        builder: TestContextBuilder<F, G>.() -> Unit
    ): NodeBuilder<F> = addChild(
        MinutestContextBuilder(
            name,
            type,
            fixtureFactory
        ).apply(builder)
    )

    private fun <T : NodeBuilder<F>> addChild(child: T): T {
        children.add(child)
        return child
    }

    override fun afterAll(f: () -> Unit) {
        afterAlls.add(f)
    }

    override fun buildNode(): Node<PF> = PreparedContext(
        name,
        children.map { it.buildNode() },
        annotations,
        befores,
        afters,
        afterAlls,
        checkedFixtureFactory()
    ).transformedBy(transforms)

    private fun checkedFixtureFactory(): (PF, TestDescriptor) -> F = when {
        // broken out for debugging
        thisContextDoesntNeedAFixture() ->
            fixtureFactory
        fixtureFactory.type == this.type ->
            fixtureFactory
        else ->
            error("Fixture has not been set in context \"$name\"")
    }

    private fun thisContextDoesntNeedAFixture() =
        befores.isEmpty() && afters.isEmpty() && children.filterIsInstance<TestBuilder<F>>().isEmpty()
}


