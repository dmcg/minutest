package dev.minutest.internal

import dev.minutest.Context
import dev.minutest.Node
import dev.minutest.Test
import dev.minutest.TestDescriptor

/**
 * Create the root [RunnableNode] for a tree of tests.
 */
internal fun Node<Unit>.toRootRunnableNode(): RunnableNode = toRunnableNode(RootExecutor)

/**
 * RunnableNodes are a view of the [Node] tree provided to hide the gory details
 * of fixture types and [TestExecutor]s from test runners.
 */
sealed class RunnableNode(
) {
    abstract val testDescriptor: TestDescriptor
    abstract val name: String
    abstract val sourceReference: SourceReference?
}

internal class RunnableTest internal constructor(
    private val test: Test<*>,
    override val testDescriptor: TestDescriptor,
    private val f: () -> Unit
) : RunnableNode() {

    override val name get() = test.name

    override val sourceReference get() =
        test.markers.filterIsInstance<SourceReference>().firstOrNull()

    fun invoke() {
        f()
    }
}

internal class RunnableContext(
    private val context: Context<*, *>,
    val children: List<RunnableNode>,
    override val testDescriptor: TestDescriptor
) : RunnableNode() {

    override val name get() = context.name

    override val sourceReference get() =
        context.markers.filterIsInstance<SourceReference>().firstOrNull()
}

private fun <F> Node<F>.toRunnableNode(executor: TestExecutor<F>): RunnableNode =
    when (this) {
        is Test<F> -> this.toRunnableTest(executor)
        is Context<F, *> -> this.toRunnableContext(executor)
    }

private fun <F> Test<F>.toRunnableTest(executor: TestExecutor<F>) =
    RunnableTest(this, executor) { executor.runTest(this) }

private fun <PF, F> Context<PF, F>.toRunnableContext(
    executor: TestExecutor<PF>
): RunnableContext {
    val childExecutor = executor.andThen(this) // there has to be just one of these
    // as they hold state about what tests in a context have been run

    return RunnableContext(
        this,
        children.map { it.toRunnableNode(childExecutor) },
        executor
    )
}