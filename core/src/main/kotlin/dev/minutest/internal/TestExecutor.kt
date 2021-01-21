package dev.minutest.internal

import dev.minutest.*
import dev.minutest.internal.ContextExecutor.ContextState.*

/**
 * The TestExecutor is built as the test-running infrastructure traverses down the
 * context tree. So at a [Context], there is an executor which has a parent which
 * is an executor for the parent context and so up back to the singleton [RootExecutor].
 *
 * This saves [Context]s knowing their parent, and the difficulty of maintaining the
 * relationship as we add wrapping contexts.
 */
internal interface TestExecutor<F> : TestDescriptor {

    fun runTest(test: Test<F>)

    // Run a testlet, which may be a test, or represent a sub-contexts test
    fun runTest(testDescriptor: TestDescriptor, testlet: Testlet<F>)

    // Compose with a child context to get an executor for that child
    fun <G> andThen(childContext: Context<F, G>): TestExecutor<G> =
        ContextExecutor(this, childContext)
}

internal class ContextExecutor<PF, F>(
    override val parent: TestExecutor<PF>,
    private val context: Context<PF, F>
) : TestExecutor<F> {
    override val name get() = context.name

    private var state = UNOPENED

    override fun runTest(test: Test<F>) {
        runTest(this.andThenName(test.name), test)
    }

    /**
     * The parent context's fixture, before, and after code is invoked by getting the
     * parent executor to run a [Testlet] which in turn asks our context to run a test.
     */
    override fun runTest(
        testDescriptor: TestDescriptor,
        testlet: Testlet<F>
    ) {
        parent.runTest(testDescriptor) { fixture, _ /* 1 */ ->
            maybeOpen(this) /* 2 */
            context.runTest(
                testlet.translatingAssumptions(),
                fixture,
                testDescriptor
            )
            fixture
        }

        // 1 - NB use the testDescriptor supplied to this runTest so that we always see
        // the longest path - the one the root to this context.

        // 2 - We ask our the parent to try to open us - that way the outermost context
        // is opened first.
    }

    private fun maybeOpen(testDescriptor: TestDescriptor) {
        synchronized(this) {
            if (state == UNOPENED) {
                context.open(testDescriptor)
                state = OPENED
            }
        }
    }

    private enum class ContextState {
        UNOPENED, OPENED
    }
}

/**
 * The root executor has no [Context], it just supplies the [Unit] fixture.
 */
internal object RootExecutor : TestExecutor<Unit>, RootDescriptor {
    override val name = "ROOT"
    override val parent: Nothing? = null

    override fun runTest(
        testDescriptor: TestDescriptor,
        testlet: Testlet<Unit>
    ) {
        testlet(Unit, testDescriptor)
    }

    override fun runTest(test: Test<Unit>) {
        // this ends up being called if you SKIP the root test,
        // as we substitute the context with a test that throws!
        runTest(this.andThenName(test.name), test)
    }
}

internal fun TestDescriptor.andThenName(name: String): TestDescriptor = object : TestDescriptor {
    override val name = name
    override val parent: TestDescriptor = this@andThenName
}