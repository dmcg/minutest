package dev.minutest.internal

import dev.minutest.Context
import dev.minutest.Node
import dev.minutest.Test
import java.util.concurrent.ExecutorService
import java.util.concurrent.ForkJoinPool

private val pool: ForkJoinPool? = when {
    System.getProperty("dev.minutest.parallel") != null -> {
        System.err.println("Minutest using parallel execution")
        ForkJoinPool.commonPool()
    }
    else -> null
}

/**
 * Create a [RunnableContext] representing the roots of a tree of tests.
 */
internal fun Context<Unit, *>.toRootContext(
    executorService: ExecutorService? = pool
): RunnableContext =
    when (this) {
        is AmalgamatedRootContext -> this.toRunnableContext(executorService)
        else -> this.toRunnableContext(RootExecutor, executorService)
    }

// In this case we need to build a RunnableContext where the children
// are themselves roots (have the RootExecutor rather than one started
// at this level).
private fun AmalgamatedRootContext.toRunnableContext(
    executorService: ExecutorService?
) = RunnableContext(
    RootExecutor.andThenName(name),
    Sequence {
        children.map {
            it.toRunnableNode(RootExecutor, executorService)
        }.iterator() },
    this
)

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
        children.map {
            it.toRunnableNode(childExecutor, executorService)
        }.asSequence(),
        this
    )
}

private fun RunnableTest.maybeEager(executorService: ExecutorService?): RunnableTest =
    executorService?.let { this.asEager(it) } ?: this

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