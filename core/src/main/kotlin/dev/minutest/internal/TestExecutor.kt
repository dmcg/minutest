package dev.minutest.internal

import dev.minutest.*

/**
 * The TestExecutor is built by running down the context tree. It can then run a test by asking the contexts up the
 * tree to supply their fixture.
 */
interface TestExecutor<F> : TestDescriptor {

    fun runTest(test: Test<F>) {
        runTest(test, this.andThenJust(test.name))
    }

    fun runTest(testlet: Testlet<F>, testDescriptor: TestDescriptor)

    fun <G> andThen(nextContext: Context<F, G>): TestExecutor<G> = object: TestExecutor<G> {
        override val name = nextContext.name
        override val parent = this@TestExecutor

        override fun runTest(testlet: Testlet<G>, testDescriptor: TestDescriptor) {
            // NB use the top testDescriptor so that we always see the longest path - the one from the
            // bottom of the stack.
            val testletForParent: Testlet<F> = { fixture, _ ->
                nextContext.runTest(testlet, fixture, testDescriptor)
                fixture
            }
            return parent.runTest(testletForParent, testDescriptor)
        }
    }
}

internal object RootExecutor : TestExecutor<Unit>, RootDescriptor {
    override val name = ""
    override val parent: Nothing? = null
    override fun runTest(testlet: Testlet<Unit>, testDescriptor: TestDescriptor): Unit = testlet(Unit, testDescriptor)
}

private fun TestExecutor<*>.andThenJust(name: String): TestDescriptor = object : TestDescriptor {
    override val name: String = name
    override val parent = this@andThenJust
}

