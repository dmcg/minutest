package dev.minutest.experimental

import dev.minutest.Context
import dev.minutest.TestDescriptor
import org.opentest4j.IncompleteExecutionException
import org.opentest4j.TestAbortedException

interface TestEventListener {
    fun <PF, F> contextOpened(context: Context<PF, F>, testDescriptor: TestDescriptor)
    fun <F> testStarting(fixture: F, testDescriptor: TestDescriptor) {}
    fun <F> testComplete(fixture: F, testDescriptor: TestDescriptor) {}
    fun <F> testSkipped(fixture: F, testDescriptor: TestDescriptor, t: IncompleteExecutionException) {}
    fun <F> testAborted(fixture: F, testDescriptor: TestDescriptor, t: TestAbortedException) {}
    fun <F> testFailed(fixture: F, testDescriptor: TestDescriptor, t: Throwable) {}
    fun <PF, F> contextClosed(context: Context<PF, F>, testDescriptor: TestDescriptor) {}
}