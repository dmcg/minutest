package dev.minutest.internal

import dev.minutest.Context
import dev.minutest.Node
import dev.minutest.Test
import dev.minutest.TestDescriptor

/**
 * RunnableNodes are a view of the [Node] tree provided to hide the gory details
 * of fixture types and [TestExecutor]s from test runners.
 */
sealed class RunnableNode {
    val name: String get() = testDescriptor.name
    abstract val testDescriptor: TestDescriptor
    abstract val markers: List<Any>
}

internal data class RunnableTest(
    override val testDescriptor: TestDescriptor,
    private val test: Test<*>,
    private val f: () -> Unit
) : RunnableNode(), () -> Unit {

    override val markers get() = test.markers

    override operator fun invoke() {
        f()
    }

    override fun toString() =
        "RunnableTest ${testDescriptor.pathAsString()}"
}

internal class RunnableContext(
    override val testDescriptor: TestDescriptor,
    val children: List<RunnableNode>,
    private val context: Context<*, *>
) : RunnableNode() {

    override val markers get() = context.markers

    fun close() {
        context.close(testDescriptor)
    }
}

val RunnableNode.sourceReference
    get() = markers.filterIsInstance<SourceReference>().firstOrNull()

