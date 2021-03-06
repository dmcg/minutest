package dev.minutest.internal

import dev.minutest.*
import dev.minutest.experimental.transformedBy
import java.io.File

/**
 * Internal state behind the DSL.
 */
internal data class MinutestContextBuilder<PF, F>(
    val name: String,
    private val parentFixtureType: FixtureType,
    private val fixtureType: FixtureType,
    private var fixtureFactory: FixtureFactory<PF, F>,
    private val children: MutableList<NodeBuilder<F>> = mutableListOf(),
    private val beforeAlls: MutableList<(TestDescriptor) -> Unit> = mutableListOf(),
    private val befores: MutableList<(F, TestDescriptor) -> F> = mutableListOf(),
    private val afters: MutableList<(FixtureValue<F>, TestDescriptor) -> Unit> = mutableListOf(),
    private val afterAlls: MutableList<(TestDescriptor) -> Unit> = mutableListOf(),
    private val markers: MutableList<Any> = mutableListOf(),
    private val transforms: MutableList<NodeTransform<PF>> = mutableListOf(),
    private val block: TestContextBuilder<PF, F>.() -> Unit
) : TestContextBuilder<PF, F>(), NodeBuilder<PF> {

    override fun setFixtureFactory(factory: (testDescriptor: TestDescriptor) -> F) {
        if (fixtureFactory is ExplicitFixtureFactory)
            throw IllegalStateException("Fixture already set in context \"$name\"")
        fixtureFactory =
            ExplicitFixtureFactory(parentFixtureType, fixtureType) { _, testDescriptor ->
                factory(testDescriptor)
            }
    }

    override fun setDerivedFixtureFactory(
        factory: (parentFixture: PF, testDescriptor:TestDescriptor) -> F
    ) {
        when {
            fixtureFactory is ExplicitFixtureFactory ->
                throw IllegalStateException("Fixture already set in context \"$name\"")
            fixtureFactory.outputType.isSubtypeOf(parentFixtureType) ->
                fixtureFactory = ExplicitFixtureFactory(
                    parentFixtureType, fixtureType, factory)
            else ->
                error(
                    "You can't deriveFixture in context \"$name\" " +
                        "because the parent context has no fixture"
                )
        }
    }

    override fun addBefore(transform: (F, TestDescriptor) -> F) {
        befores.add(transform)
    }

    override fun addAfter(
        operation: (FixtureValue<F>, TestDescriptor) -> Unit
    ) {
        afters.add(operation)
    }

    override fun addTest(
        name: String,
        f: (F, TestDescriptor) -> F
    ): Annotatable<F> = addChild(TestBuilder(name, f))

    private fun NodeBuilder<F>.withMarkerForBlockInvocation(): NodeBuilder<F> {
        return apply { sourceReferenceForBlockInvocation()?.let { addMarker(it) } }
    }

    override fun addContext(
        name: String,
        block: TestContextBuilder<F, F>.() -> Unit
    ): Annotatable<F> {
        return addContext(
            name,
            fixtureType,
            if (fixtureFactory.isCompatibleWith(fixtureType, fixtureType))
                IdFixtureFactory(fixtureType)
            else
                UnsafeFixtureFactory(fixtureFactory.outputType),
            block
        )
    }

    override fun <G> addDerivedContext(
        name: String,
        newFixtureType: FixtureType,
        block: TestContextBuilder<F, G>.() -> Unit
    ): Annotatable<F> = addContext(
        name,
        newFixtureType,
        UnsafeFixtureFactory(fixtureType),
        block
    )

    override fun addMarker(marker: Any) {
        markers.add(marker)
    }

    override fun addTransform(transform: NodeTransform<PF>) {
        transforms.add(transform)
    }

    private fun <G> addContext(
        name: String,
        newFixtureType: FixtureType,
        fixtureFactory: FixtureFactory<F, G>,
        block: TestContextBuilder<F, G>.() -> Unit
    ): Annotatable<F> {
        return addChild(
            MinutestContextBuilder(
                name,
                this.fixtureType,
                newFixtureType,
                fixtureFactory,
                block = block
            )
        )
    }

    private fun <T : NodeBuilder<F>> addChild(child: T): T {
        children.add(child.withMarkerForBlockInvocation())
        return child
    }

    override fun addBeforeAll(f: (TestDescriptor) -> Unit) {
        beforeAlls.add(f)
    }

    override fun addAfterAll(f: (TestDescriptor) -> Unit) {
        afterAlls.add(f)
    }

    override fun buildNode(): Node<PF> = this.apply(block).run {
        PreparedContext(
            name,
            children.map { it.buildNode() },
            markers,
            NodeId.forBuilder(this),
            parentFixtureType,
            fixtureType,
            beforeAlls,
            befores,
            afters,
            afterAlls,
            checkedFixtureFactory()
        ).transformedBy(transforms)
    }

    private fun checkedFixtureFactory(): (PF, TestDescriptor) -> F = when {
        // broken out for debugging
        fixtureFactory is ExplicitFixtureFactory ->
            fixtureFactory
        fixtureFactory.isCompatibleWith(parentFixtureType, fixtureType) ->
            fixtureFactory
        thisContextDoesntReferenceTheFixture() ->
            fixtureFactory
        else ->
            error("Fixture has not been set in context \"$name\"")
    }

    private fun thisContextDoesntReferenceTheFixture() =
        befores.isEmpty() &&
            afters.isEmpty() &&
            !children.any { it is TestBuilder<F> }
}

private val sourceRoot = listOf(
    File("src/test/kotlin"),
    File("src/test/java")
).find { it.isDirectory } ?: File(".")

private fun sourceReferenceForBlockInvocation(): SourceReference? {
    val elements = Thread.currentThread().stackTrace
    return elements.drop(2).find {
        val string = it.toString()
        !string.startsWith("dev.minutest.internal")
            && !string.startsWith("dev.minutest.TestContextBuilder")
    }?.toSourceReference(sourceRoot)
}

private fun StackTraceElement.toSourceReference(sourceRoot: File): SourceReference? {
    val fileName = fileName ?: return null
    val type = Class.forName(className)
    return SourceReference(
        sourceRoot.toPath()
            .resolve(type.`package`.name.replace(".", "/"))
            .resolve(fileName).toFile()
            .absolutePath,
        lineNumber
    )
}

