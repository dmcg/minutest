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
    private val afters: List<(F, TestDescriptor) -> Unit>,
    private var afterAlls: List<() -> Unit>,
    private val fixtureFactory: (PF, TestDescriptor) -> F,
    override val annotations: MutableList<TestAnnotation>
) : Context<PF, F>() {

    override fun runTest(testlet: Testlet<F>, parentFixture: PF, testDescriptor: TestDescriptor): F {
        val fixture = fixtureFactory(parentFixture, testDescriptor)
        return applyBeforesTo(fixture, testDescriptor)
            .tryMap { f -> testlet(f, testDescriptor) }
            .onLastValue { applyAftersTo(it, testDescriptor) }
            .orThrow()
    }

    override fun close() {
        afterAlls.forEach {
            it()
        }
    }

    private fun applyAftersTo(fixture: F, testDescriptor: TestDescriptor) {
        afters.forEach { afterFn ->
            afterFn(fixture, testDescriptor)
        }
    }

    override fun withTransformedChildren(transform: NodeTransform) = copy(children = transform.applyTo(children))

    // apply befores in order - if anything is thrown return it and the last successful value
    private fun applyBeforesTo(fixture: F, testDescriptor: TestDescriptor): SequenceResult<F> =
        befores.fold(SequenceResult(null, fixture)) { soFar: SequenceResult<F>, before: (F, TestDescriptor) -> F ->
            try {
                SequenceResult(null, before(soFar.lastValue, testDescriptor))
            } catch (t: Throwable) {
                // I'll get my coat
                return SequenceResult(t, fixture)
            }
        }
}