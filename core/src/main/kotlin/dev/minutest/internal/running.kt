package dev.minutest.internal

import dev.minutest.Context
import dev.minutest.Node
import dev.minutest.Test
import java.util.concurrent.ExecutorService


internal fun Node<Unit>.toRootContext(
): RunnableContext =
    when (this) {
        is AmalgamatedRootContext ->
            // In this case we need to build a RunnableContext where the children
            // are themselves roots
            RunnableContext(
                RootExecutor,
                this.children.map { it.toRootRunnableNode() },
                this
            )
        else -> when (val runnableNode = toRootRunnableNode()) {
            is RunnableContext -> runnableNode
            is RunnableTest -> TODO("Root is test")
        }
    }


internal fun Node<Unit>.toRootRunnableNodes(
): List<RunnableNode> =
    when (this) {
        is AmalgamatedRootContext -> this.children.map { it.toRootRunnableNode() }
        else -> when (val runnableNode = toRootRunnableNode()) {
            is RunnableTest -> listOf(runnableNode)
            is RunnableContext -> runnableNode.children
        }
    }

/**
 * Create the root [RunnableNode] for a tree of tests.
 */
internal fun Node<Unit>.toRootRunnableNode(
): RunnableNode =
    toRunnableNode(RootExecutor)

private fun <F> Node<F>.toRunnableNode(
    executor: TestExecutor<F>,
): RunnableNode =
    when (this) {
        is Test<F> -> this.toRunnableTest(executor)
        is Context<F, *> -> this.toRunnableContext(executor)
    }

private fun <F> Test<F>.toRunnableTest(
    executor: TestExecutor<F>,
): RunnableTest {
    return RunnableTest(executor, this) { executor.runTest(this) }
}

private fun <PF, F> Context<PF, F>.toRunnableContext(
    executor: TestExecutor<PF>,
): RunnableContext {
    val childExecutor = executor.andThen(this) // there has to be just one of these
    // as they hold state about what tests in a context have been run
    return RunnableContext(
        executor,
        children.map { it.toRunnableNode(childExecutor) },
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