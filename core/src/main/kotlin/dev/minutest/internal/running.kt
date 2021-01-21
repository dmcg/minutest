package dev.minutest.internal

import dev.minutest.Context
import dev.minutest.Node
import dev.minutest.Test
import java.util.concurrent.ExecutorService
import java.util.concurrent.ForkJoinPool

private val pool = ForkJoinPool()

/**
 * Create a [RunnableContext] representing the roots of a tree of tests.
 */
internal fun Node<Unit>.toRootContext(
    executorService: ExecutorService? = pool
): RunnableContext =
    when (this) {
        is AmalgamatedRootContext ->
            // In this case we need to build a RunnableContext where the children
            // are themselves roots
            RunnableContext(
                RootExecutor.andThenName(this.name), // never used
                this.children.map { it.toRunnableNode(RootExecutor, executorService) },
                this
            )
        else -> when (val runnableNode = toRunnableNode(RootExecutor, executorService)) {
            is RunnableContext -> runnableNode
            is RunnableTest -> TODO("Root is test")
        }
    }

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
    executorService: ExecutorService? = null
): RunnableTest =
    RunnableTest(executor.andThenName(this.name), this) {
        executor.runTest(this)
    }.maybeEager(executorService)

private fun <PF, F> Context<PF, F>.toRunnableContext(
    executor: TestExecutor<PF>,
    executorService: ExecutorService? = null
): RunnableContext {
    val childExecutor = executor.andThen(this) // there has to be just one of these
    // as they hold state about what tests in a context have been run
    return RunnableContext(
        executor.andThenName(name),
        children.map { it.toRunnableNode(childExecutor, executorService) },
        this
    )
}

private fun RunnableTest.maybeEager(executorService: ExecutorService?): RunnableTest =
    executorService?.let { this.asEager(it) } ?: this

private fun RunnableTest.asEager(executorService: ExecutorService): RunnableTest {
    println("Eagerly executing ${this.testDescriptor.pathAsString()}")
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