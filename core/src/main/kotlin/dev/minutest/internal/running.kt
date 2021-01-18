package dev.minutest.internal

import dev.minutest.Context
import dev.minutest.Node
import dev.minutest.Test
import java.util.concurrent.ExecutorService

internal fun Node<Unit>.toRootRunnableNodes(
    executorService: ExecutorService? = null
): List<RunnableNode> =
    when (this) {
        is AmalgamatedRootContext -> this.children.map { it.toRootRunnableNode(executorService) }
        else -> when (val runnableNode = toRootRunnableNode(executorService)) {
            is RunnableTest -> listOf(runnableNode)
            is RunnableContext -> runnableNode.children
        }
    }
/**
 * Create the root [RunnableNode] for a tree of tests.
 */
internal fun Node<Unit>.toRootRunnableNode(
    executorService: ExecutorService? = null
): RunnableNode =
    toRunnableNode(RootExecutor, executorService)

private fun <F> Node<F>.toRunnableNode(
    executor: TestExecutor<F>,
    executorService: ExecutorService? = null
): RunnableNode =
    when (this) {
        is Test<F> -> this.toRunnableTest(executor, executorService)
        is Context<F, *> -> this.toRunnableContext(executor, executorService)
    }

private fun <F> Test<F>.toRunnableTest(
    executor: TestExecutor<F>,
    executorService: ExecutorService?
): RunnableTest {
    val base = RunnableTest(executor, this) { executor.runTest(this) }
    return if (executorService == null) base else base.asEager(executorService)
}

private fun <PF, F> Context<PF, F>.toRunnableContext(
    executor: TestExecutor<PF>,
    executorService: ExecutorService?
): RunnableContext {
    val childExecutor = executor.andThen(this) // there has to be just one of these
    // as they hold state about what tests in a context have been run

    return RunnableContext(
        executor,
        children.map { it.toRunnableNode(childExecutor, executorService) },
        this
    )
}

private fun RunnableTest.asEager(executorService: ExecutorService): RunnableTest {
    var exception: Throwable? = null
    val future = executorService.submit {
        try {
            this.invoke()
        } catch (x: Throwable) {
            exception = x
        }
    }
    return this.copy(f = {
        future.get()
        exception?.let { throw it }
    })
}