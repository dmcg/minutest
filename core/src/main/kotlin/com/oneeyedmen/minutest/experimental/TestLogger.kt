package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.TestDescriptor
import org.opentest4j.TestAbortedException
import org.opentest4j.TestSkippedException

class TestLogger(val log: MutableList<String> = mutableListOf()) : TestEventListener {

    private var lastPath = emptyList<String>()

    override fun <F> testStarting(fixture: F, testDescriptor: TestDescriptor) = Unit

    override fun <F> testComplete(fixture: F, testDescriptor: TestDescriptor) {
        log(testDescriptor)
    }

    override fun <F> testFailed(fixture: F, testDescriptor: TestDescriptor, t: Throwable) {
        log(testDescriptor)
    }

    override fun <F> testAborted(fixture: F, testDescriptor: TestDescriptor, t: TestAbortedException) {
        log(testDescriptor)
    }

    override fun <F> testSkipped(fixture: F, testDescriptor: TestDescriptor, t: TestSkippedException) {
        log(testDescriptor)
    }

    override fun <PF, F> contextClosed(runtimeContext: RuntimeContext<PF, F>) = Unit

    private fun log(testDescriptor: TestDescriptor) {
        val path = testDescriptor.fullName()
        val commonPrefix = lastPath.commonPrefix(path)
        path.subList(commonPrefix.size, path.size).forEachIndexed { i, name ->
            log.add((commonPrefix.size + i).tabs() + name)
        }
        lastPath = path
    }
}

private fun <E> List<E>.commonPrefix(that: List<E>): List<E> =
    this.zip(that).takeWhile { (a, b) -> a == b }.map { (a, _) -> a }

private fun Int.tabs() = "\t".repeat(this)