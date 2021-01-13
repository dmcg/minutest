package dev.minutest.internal

import dev.minutest.Context
import dev.minutest.Node
import dev.minutest.Test


internal sealed class RunnableNode<F> {
    abstract val name: String
}

internal class RunnableTest<F>(
    internal val test: Test<F>,
    private val testExecutor: TestExecutor<F>
) : RunnableNode<F>() {
    override val name get() = test.name

    fun invoke() {
        testExecutor.runTest(test)
    }
}

internal class RunnableContext<PF, F>(
    internal val context: Context<PF, F>,
    val children: List<RunnableNode<F>>
) : RunnableNode<PF>() {
    override val name get() = context.name
}

internal fun <F> Node<F>.toRunnableNode(executor: TestExecutor<F>): RunnableNode<F> =
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
        children.map { it.toRunnableNode(childExecutor) }
    )
}