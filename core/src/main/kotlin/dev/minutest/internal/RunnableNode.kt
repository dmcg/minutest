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
    abstract val name: String
    abstract val testDescriptor: TestDescriptor
    abstract val markers: List<Any>
}

internal data class RunnableTest(
    override val testDescriptor: TestDescriptor,
    private val test: Test<*>,
    private val f: () -> Unit
) : RunnableNode(), () -> Unit {

    override val name get() = test.name
    override val markers get() = test.markers

    override operator fun invoke() {
        f()
    }

    override fun toString(): String {
        return "Runnable test with name $name and path ${testDescriptor.pathAsString()}"
    }
}

internal class RunnableContext(
    override val testDescriptor: TestDescriptor,
    val children: List<RunnableNode>,
    private val context: Context<*, *>
) : RunnableNode() {

    override val name get() = context.name
    override val markers get() = context.markers

    fun close() {
        context.close(testDescriptor.andThenTestName(name))
    }
}

val RunnableNode.sourceReference get() =
    markers.filterIsInstance<SourceReference>().firstOrNull()

