package dev.minutest.experimental

import dev.minutest.Context
import dev.minutest.Node
import dev.minutest.Test
import org.opentest4j.TestAbortedException
import org.opentest4j.TestSkippedException


fun <F> Node<F>.telling(listener: TestEventListener): Node<F> =
    when (this) {
        is Test<F> -> this.telling(listener)
        is Context<F, *> -> this.telling(listener)
    }

fun <PF, F> Context<PF, F>.telling(listener: TestEventListener): Context<PF, F> =
    ContextWrapper(this,
        onOpen = { listener.contextOpened(this, it) },
        children = children.map { it.telling(listener) },
        onClose = { listener.contextClosed(this, it) }
    )

private fun <F> Test<F>.telling(listener: TestEventListener) = copy(
    f = { fixture, testDescriptor ->
        listener.testStarting(this, fixture, testDescriptor)
        try {
            this(fixture, testDescriptor).also {
                listener.testComplete(this, fixture, testDescriptor)
            }
        } catch (skipped: TestSkippedException) {
            listener.testSkipped(this, fixture, testDescriptor, skipped)
            throw skipped
        } catch (skipped: MinutestSkippedException) {
            listener.testSkipped(this, fixture, testDescriptor, skipped)
            throw skipped
        } catch (aborted: TestAbortedException) {
            listener.testAborted(this, fixture, testDescriptor, aborted)
            throw aborted
        } catch (t: Throwable) {
            listener.testFailed(this, fixture, testDescriptor, t)
            throw t
        }
    }
)
