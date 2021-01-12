package dev.minutest.internal

import dev.minutest.*

/**
 * The TestExecutor is built as the test running infrastructure traverses down the
 * context tree.
 */
internal interface TestExecutor<F> : TestDescriptor {

    fun runTest(test: Test<F>) {
        runTest(
            test,
            this.andThenTestName(test.name)
        )
    }

    fun runTest(testlet: Testlet<F>, testDescriptor: TestDescriptor)

    /**
     * The parent context's fixture, before, and after code is invoked by creating a
     * [Testlet] representing execution of a test in the child context and asking the
     * parent to run it.
     */
    fun <G> andThen(childContext: Context<F, G>): TestExecutor<G> =
        object : TestExecutor<G> {
            override val name = childContext.name
            override val parent = this@TestExecutor

            override fun runTest(
                testlet: Testlet<G>,
                testDescriptor: TestDescriptor
            ) {
                // NB use the root testDescriptor so that we always see the longest path -
                // the one from most nested context.
                val testletForParent: Testlet<F> = { fixture, _ ->
                    childContext.runTest(testlet, fixture, testDescriptor)
                    fixture
                }
                return parent.runTest(testletForParent, testDescriptor)
            }
        }
}

/**
 * The root executor has no [Context], it just supplies the [Unit] fixture.
 */
internal object RootExecutor : TestExecutor<Unit>, RootDescriptor {
    override val name = ""
    override val parent: Nothing? = null
    override fun runTest(
        testlet: Testlet<Unit>,
        testDescriptor: TestDescriptor
    ): Unit =
        testlet(Unit, testDescriptor)
}

internal fun TestDescriptor.andThenTestName(name: String): TestDescriptor = object : TestDescriptor {
    override val name = name
    override val parent: TestDescriptor = this@andThenTestName
}


