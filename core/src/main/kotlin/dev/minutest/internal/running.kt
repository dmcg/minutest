package dev.minutest.internal

import dev.minutest.Context
import dev.minutest.Node
import dev.minutest.Test

/**
 * Create the root [RunnableNode] for a tree of tests.
 */
internal fun Node<Unit>.toRootRunnableNode(): RunnableNode = toRunnableNode(RootExecutor)

private fun <F> Node<F>.toRunnableNode(executor: TestExecutor<F>): RunnableNode =
    when (this) {
        is Test<F> -> this.toRunnableTest(executor)
        is Context<F, *> -> this.toRunnableContext(executor)
    }

private fun <F> Test<F>.toRunnableTest(executor: TestExecutor<F>) =
    RunnableTest(executor, this) { executor.runTest(this) }

private fun <PF, F> Context<PF, F>.toRunnableContext(
    executor: TestExecutor<PF>
): RunnableContext {
    val childExecutor = executor.andThen(this) // there has to be just one of these
    // as they hold state about what tests in a context have been run

    return RunnableContext(
        executor,
        children.map { it.toRunnableNode(childExecutor) },
        this
    )
}