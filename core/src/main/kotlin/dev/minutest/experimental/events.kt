package dev.minutest.experimental

import dev.minutest.Context
import dev.minutest.Node
import dev.minutest.Test
import dev.minutest.TestDescriptor
import org.opentest4j.IncompleteExecutionException
import org.opentest4j.TestAbortedException
import org.opentest4j.TestSkippedException


interface TestEventListener {
    fun <F> testStarting(fixture: F, testDescriptor: TestDescriptor) {}
    fun <F> testComplete(fixture: F, testDescriptor: TestDescriptor) {}
    fun <F> testSkipped(fixture: F, testDescriptor: TestDescriptor, t: IncompleteExecutionException) {}
    fun <F> testAborted(fixture: F, testDescriptor: TestDescriptor, t: TestAbortedException) {}
    fun <F> testFailed(fixture: F, testDescriptor: TestDescriptor, t: Throwable) {}
    fun <PF, F> contextClosed(context: Context<PF, F>) {}
}

class Telling<F>(private val listener: TestEventListener) : TransformingAnnotation<F>( { it.telling(listener) } )

fun <F, NodeT: Node<F>> telling(listener: TestEventListener): (NodeT) -> NodeT = { node ->
    @Suppress("UNCHECKED_CAST")
    node.telling(listener) as NodeT
}

private fun <PF, F> Context<PF, F>.telling(listener: TestEventListener): Context<PF, F> =
    ContextWrapper(this,
        children = children.map { it.telling(listener) },
        onClose = { listener.contextClosed(this@telling) }
    )

private fun <F> Test<F>.telling(listener: TestEventListener) = copy(
    f = { fixture, testDescriptor ->
        listener.testStarting(fixture, testDescriptor)
        try {
            this(fixture, testDescriptor).also {
                listener.testComplete(fixture, testDescriptor)
            }
        } catch (skipped: TestSkippedException) {
            listener.testSkipped(fixture, testDescriptor, skipped)
            throw skipped
        } catch (skipped: MinutestSkippedException) {
            listener.testSkipped(fixture, testDescriptor, skipped)
            throw skipped
        } catch (aborted: TestAbortedException) {
            listener.testAborted(fixture, testDescriptor, aborted)
            throw aborted
        } catch (t: Throwable) {
            listener.testFailed(fixture, testDescriptor, t)
            throw t
        }
    }
)

private fun <F> Node<F>.telling(listener: TestEventListener): Node<F> =
    when (this) {
        is Test<F> -> this.telling(listener)
        is Context<F, *> -> this.telling(listener)
    }

