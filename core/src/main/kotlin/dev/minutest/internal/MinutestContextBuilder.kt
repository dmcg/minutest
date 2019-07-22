package dev.minutest.internal

import dev.minutest.*
import dev.minutest.experimental.transformedBy

/**
 * Internal implementation of [TestContextBuilder] which hides the details and the [NodeBuilder]ness.
 */
internal data class MinutestContextBuilder<PF, F>(
    val name: String,
    private val parentFixtureType: FixtureType,
    private val fixtureType: FixtureType,
    private var fixtureFactory: FixtureFactory<PF, F>,
    private var explicitFixtureFactory: Boolean = false,
    private val children: MutableList<NodeBuilder<F>> = mutableListOf(),
    private val befores: MutableList<(F, TestDescriptor) -> F> = mutableListOf(),
    private val afters: MutableList<(FixtureValue<F>, TestDescriptor) -> Unit> = mutableListOf(),
    private val afterAlls: MutableList<() -> Unit> = mutableListOf(),
    private val markers: MutableList<Any> = mutableListOf(),
    private val transforms: MutableList<NodeTransform<PF>> = mutableListOf()
) : TestContextBuilder<PF, F>(), NodeBuilder<PF> {

    override fun fixture(factory: Unit.(testDescriptor: TestDescriptor) -> F) {
        if (explicitFixtureFactory)
            throw IllegalStateException("Fixture already set in context \"$name\"")
        fixtureFactory = FixtureFactory(parentFixtureType, fixtureType) { _, testDescriptor ->
            Unit.factory(testDescriptor)
        }
        explicitFixtureFactory = true
    }

    override fun deriveFixture(f: (PF).(TestDescriptor) -> F) {
        if (explicitFixtureFactory)
            throw IllegalStateException("Fixture already set in context \"$name\"")
        fixtureFactory = FixtureFactory(parentFixtureType, fixtureType, f)
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

    override fun context(name: String, block: TestContextBuilder<F, F>.() -> Unit) =
        newContext(
            name,
            fixtureType,
            FixtureFactory(fixtureType, fixtureFactory.outputType) { f, _ -> f }, // [1]
            block)
    /* 1 - We don't know for sure that the fixtureType of our fixtureFactory is the same as our fixtureType, so we pass it on
       so that checkedFixtureFactory() can do the right thing.

       The value returned by the factory in our child should be the value that this context has computed -
       that's the f in the block
     */

    override fun <G> internalDerivedContext(
        name: String,
        type: FixtureType,
        block: TestContextBuilder<F, G>.() -> Unit
    ): NodeBuilder<F> = newContext(
        name,
        type,
        FixtureFactory(fixtureType, fixtureFactory.outputType) { f, _ ->
            // [2]
            @Suppress("UNCHECKED_CAST")
            f as? G ?: error("Please report sighting of wrong fixture factory")
        },
        block
    )
    /* 2 - If you're deriving a context we know might still be able to build a context, because the fixtureType may not have
       changed, or may be compatible. So we punt a bit here and let checkedFixtureFactory() do its job.
     */

    override fun addMarker(marker: Any) {
        markers.add(marker)
    }

    override fun addTransform(transform: NodeTransform<PF>) {
        transforms.add(transform)
    }

    private fun <G> newContext(
        name: String,
        newFixtureType: FixtureType,
        fixtureFactory: FixtureFactory<F, G>,
        block: TestContextBuilder<F, G>.() -> Unit
    ): NodeBuilder<F> = addChild(
        LateContextBuilder(
            name,
            this.fixtureType,
            newFixtureType,
            fixtureFactory,
            block
        )
    )

    private fun <T : NodeBuilder<F>> addChild(child: T): T {
        children.add(child)
        return child
    }

    override fun afterAll(f: () -> Unit) {
        afterAlls.add(f)
    }

    override fun buildNode(): Node<PF> {
        return PreparedContext(
            name,
            children.map { it.buildNode() },
            markers,
            befores,
            afters,
            afterAlls,
            checkedFixtureFactory()
        ).transformedBy(transforms)
    }

    private fun checkedFixtureFactory(): (PF, TestDescriptor) -> F = when {
        // broken out for debugging
        explicitFixtureFactory -> fixtureFactory
        fixtureFactory.isCompatibleWith(parentFixtureType, fixtureType) -> fixtureFactory
        thisContextDoesntNeedAFixture() -> fixtureFactory
        else ->
            error("Fixture has not been set in context \"$name\"")
    }

    private fun thisContextDoesntNeedAFixture() =
        befores.isEmpty() && afters.isEmpty() && !children.any { it is TestBuilder<F> }
}

private fun <PF, F> FixtureFactory<PF, F>.isCompatibleWith(inputType: FixtureType, outputType: FixtureType) = inputType.isSubtypeOf(this.inputType) &&
    this.outputType.isSubtypeOf(outputType)



