package dev.minutest.internal

import dev.minutest.*

/**
 * Once the DSL has been evaluated, its contexts are converted to a [PreparedContext]
 * ready to be run.
 */
internal data class PreparedContext<PF, F>(
    override val name: String,
    override val children: List<Node<F>>,
    override val markers: List<Any>,
    override val id: NodeId,
    private val parentFixtureType: FixtureType,
    private val fixtureType: FixtureType,
    private val beforeAlls: List<(TestDescriptor) -> Unit>,
    private val befores: List<(F, TestDescriptor) -> F>,
    private val afters: List<(FixtureValue<F>, TestDescriptor) -> Unit>,
    private var afterAlls: List<(TestDescriptor) -> Unit>,
    private val fixtureFactory: (PF, TestDescriptor) -> F
) : Context<PF, F>() {

    // This is used to run the tests in this context, and also to run testlets
    // representing tests in the sub-contexts, so that all this context's
    // lifecycle functions are run for them too. See [TestExecutor].
    override fun runTest(
        testlet: Testlet<F>,
        parentFixture: PF,
        testDescriptor: TestDescriptor
    ): F {
        val fixture = fixtureFactory(parentFixture, testDescriptor)
        return applyBeforesTo(fixture, testDescriptor)
            .tryMap { f -> testlet(f, testDescriptor) }
            .also { applyAftersTo(it, testDescriptor) }
            .orThrow()
    }

    override fun open(testDescriptor: TestDescriptor) {
        beforeAlls.forEach {
            it(testDescriptor)
        }
    }

    override fun close(testDescriptor: TestDescriptor) {
        afterAlls.forEach {
            it(testDescriptor)
        }
    }

    private fun applyAftersTo(result: FixtureValue<F>, testDescriptor: TestDescriptor) {
        afters.forEach { afterFn ->
            afterFn(result, testDescriptor)
        }
    }

    override fun withTransformedChildren(transform: NodeTransform<F>) = copy(
        children = children.map { transform(it) }
    )

    // apply befores in order - if anything is thrown return it and the last successful value
    private fun applyBeforesTo(fixture: F, testDescriptor: TestDescriptor): FixtureValue<F> =
        befores.fold(FixtureValue(fixture)) { soFar: FixtureValue<F>, next: (F, TestDescriptor) -> F ->
            soFar.tryMap {
                next(it, testDescriptor)
            }
        }
}


private  inline fun <F> FixtureValue<F>.flatMap(f: (F) -> FixtureValue<F>): FixtureValue<F> =
    if (error != null) this else f(this.value)

private fun <F> FixtureValue<F>.orThrow(): F {
    if (error != null) {
        throw error
    }
    return value
}

private fun <F> FixtureValue<F>.tryMap(f: (F) -> F): FixtureValue<F> =
    flatMap {
        try {
            FixtureValue(f(it), null)
        }
        catch (t: Throwable) {
            FixtureValue(it, t)
        }
    }