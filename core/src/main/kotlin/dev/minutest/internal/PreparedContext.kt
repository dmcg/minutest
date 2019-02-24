package dev.minutest.internal

import dev.minutest.*
import dev.minutest.experimental.TestAnnotation

/**
 * The runtime representation of a context.
 */
internal data class PreparedContext<PF, F> (
    override val name: String,
    override val children: List<Node<F>>,
    private val befores: List<(F, TestDescriptor) -> F>,
    private val afters: List<(FixtureValue<F>, TestDescriptor) -> Unit>,
    private var afterAlls: List<() -> Unit>,
    private val fixtureFactory: (PF, TestDescriptor) -> F,
    override val annotations: MutableList<TestAnnotation>
) : Context<PF, F>() {

    override fun runTest(testlet: Testlet<F>, parentFixture: PF, testDescriptor: TestDescriptor): F {
        val fixture = fixtureFactory(parentFixture, testDescriptor)
        return applyBeforesTo(fixture, testDescriptor)
            .tryMap { f -> testlet(f, testDescriptor) }
            .also { applyAftersTo(it, testDescriptor) }
            .orThrow()
    }

    override fun close() {
        afterAlls.forEach {
            it()
        }
    }

    private fun applyAftersTo(result: FixtureValue<F>, testDescriptor: TestDescriptor) {
        afters.forEach { afterFn ->
            afterFn(result, testDescriptor)
        }
    }

    override fun withTransformedChildren(transform: NodeTransform) = copy(children = transform.applyTo(children))

    // apply befores in order - if anything is thrown return it and the last successful value
    private fun applyBeforesTo(fixture: F, testDescriptor: TestDescriptor): FixtureValue<F> =
        befores.fold(FixtureValue(fixture)) { soFar: FixtureValue<F>, next: (F, TestDescriptor) -> F ->
            soFar.tryMap {
                next(it, testDescriptor)
            }
        }
}

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