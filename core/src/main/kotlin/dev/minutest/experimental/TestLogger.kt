package dev.minutest.experimental

import dev.minutest.Context
import dev.minutest.TestDescriptor
import org.opentest4j.IncompleteExecutionException
import org.opentest4j.TestAbortedException

class TestLogger(
    val log: MutableList<String> = mutableListOf(),
    val indent: String = "  ",
    val prefixer: (EventType) -> String = EventType::prefix
) : TestEventListener {

    val events = mutableListOf<TestEvent>()

    enum class EventType(val prefix: String) {
        CONTEXT_OPENED("▾ "),
        TEST_STARTING("⋯ "),
        TEST_COMPLETE("✓ "),
        TEST_FAILED("X "),
        TEST_ABORTED("- "),
        TEST_SKIPPED("- "),
        CONTEXT_CLOSED("▴ "),
    }

    companion object {
        val noSymbols: (EventType) -> String = { "" }
    }

    private var currentIndent = ""

    override fun <PF, F> contextOpened(context: Context<PF, F>, testDescriptor: TestDescriptor) {
        addAndLog(EventType.CONTEXT_OPENED, testDescriptor)
    }

    override fun <F> testStarting(fixture: F, testDescriptor: TestDescriptor) {
        add(EventType.TEST_STARTING, testDescriptor)
    }

    override fun <F> testComplete(fixture: F, testDescriptor: TestDescriptor) {
        addAndLog(EventType.TEST_COMPLETE, testDescriptor)
    }

    override fun <F> testFailed(fixture: F, testDescriptor: TestDescriptor, t: Throwable) {
        addAndLog(EventType.TEST_FAILED, testDescriptor)
    }

    override fun <F> testAborted(fixture: F, testDescriptor: TestDescriptor, t: TestAbortedException) {
        addAndLog(EventType.TEST_ABORTED, testDescriptor)
    }

    override fun <F> testSkipped(fixture: F, testDescriptor: TestDescriptor, t: IncompleteExecutionException) {
        addAndLog(EventType.TEST_SKIPPED, testDescriptor)
    }

    override fun <PF, F> contextClosed(context: Context<PF, F>, testDescriptor: TestDescriptor) {
        currentIndent = currentIndent.substring(indent.length)
        add(EventType.CONTEXT_CLOSED, testDescriptor)
    }

    private fun addAndLog(eventType: EventType, testDescriptor: TestDescriptor) {
        add(eventType, testDescriptor)
        log(eventType, testDescriptor)
    }

    private fun add(eventType: EventType, testDescriptor: TestDescriptor) {
        events.add(TestEvent(eventType, testDescriptor))
    }

    private fun log(eventType: EventType, testDescriptor: TestDescriptor) {
        log.add(currentIndent + prefixer(eventType) + testDescriptor.name)
        if (eventType == EventType.CONTEXT_OPENED) {
            currentIndent += indent
        }
    }

    data class TestEvent(val eventType: EventType, val testDescriptor: TestDescriptor)
}