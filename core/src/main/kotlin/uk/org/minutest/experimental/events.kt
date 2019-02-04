package uk.org.minutest.experimental

import org.opentest4j.IncompleteExecutionException
import org.opentest4j.TestAbortedException
import org.opentest4j.TestSkippedException
import uk.org.minutest.NodeTransform
import uk.org.minutest.TestDescriptor
import uk.org.minutest.internal.ContextWrapper


interface TestEventListener {
    fun <F> testStarting(fixture: F, testDescriptor: TestDescriptor) {}
    fun <F> testComplete(fixture: F, testDescriptor: TestDescriptor) {}
    fun <F> testSkipped(fixture: F, testDescriptor: TestDescriptor, t: IncompleteExecutionException) {}
    fun <F> testAborted(fixture: F, testDescriptor: TestDescriptor, t: TestAbortedException) {}
    fun <F> testFailed(fixture: F, testDescriptor: TestDescriptor, t: Throwable) {}
    fun <PF, F> contextClosed(context: uk.org.minutest.Context<PF, F>) {}
}

class Telling(private val listener: TestEventListener) : TestAnnotation, NodeTransform {
    override fun <F> applyTo(node: uk.org.minutest.Node<F>): uk.org.minutest.Node<F> = node.telling(listener)
}

fun <F> telling(listener: TestEventListener): (uk.org.minutest.Node<F>) -> uk.org.minutest.Node<F> = { node ->
    node.telling(listener)
}

private fun <PF, F> uk.org.minutest.Context<PF, F>.telling(listener: TestEventListener): uk.org.minutest.Context<PF, F> =
    ContextWrapper(this,
        children = children.map { it.telling(listener) },
        onClose = { listener.contextClosed(this@telling) }
    )

private fun <F> uk.org.minutest.Test<F>.telling(listener: TestEventListener) = copy(
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

private fun <F> uk.org.minutest.Node<F>.telling(listener: TestEventListener): uk.org.minutest.Node<F> =
    when (this) {
        is uk.org.minutest.Test<F> -> this.telling(listener)
        is uk.org.minutest.Context<F, *> -> this.telling(listener)
    }

