package dev.minutest.internal

import dev.minutest.Context
import dev.minutest.Node
import dev.minutest.Test
import dev.minutest.TestDescriptor

/**
 * Create the root [RunnableNode] for a tree of tests.
 */
internal fun Node<Unit>.toRootRunnableNode(): RunnableNode<Unit> = toRunnableNode(RootExecutor)


sealed class RunnableNode<F>(
    val testDescriptor: TestDescriptor
) {
    abstract val name: String
}

internal class RunnableTest<F>(
    internal val test: Test<F>,
    private val testExecutor: TestExecutor<F>
) : RunnableNode<F>(testExecutor) {
    override val name get() = test.name

    fun invoke() {
        testExecutor.runTest(test)
    }
}

internal class RunnableContext<PF, F>(
    internal val context: Context<PF, F>,
    val children: List<RunnableNode<F>>,
    testDescriptor: TestDescriptor
) : RunnableNode<PF>(testDescriptor) {
    override val name get() = context.name
}

private fun <F> Node<F>.toRunnableNode(executor: TestExecutor<F>): RunnableNode<F> =
    when (this) {
        is Test<F> -> this.toRunnableTest(executor)
        is Context<F, *> -> this.toRunnableContext(executor)
    }

internal fun <F> Test<F>.toRunnableTest(executor: TestExecutor<F>) =
    RunnableTest(this, executor)

internal fun <PF, F> Context<PF, F>.toRunnableContext(
    executor: TestExecutor<PF>
): RunnableContext<PF, F> {
    val childExecutor = executor.andThen(this) // there has to be just one of these
    // as they hold state about what tests in a context have been run

    return RunnableContext(
        this,
        children.map { it.toRunnableNode(childExecutor) },
        executor
    )
}