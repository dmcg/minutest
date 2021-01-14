package dev.minutest.experimental

import dev.minutest.Context
import dev.minutest.TestDescriptor
import org.opentest4j.IncompleteExecutionException
import org.opentest4j.TestAbortedException

class TestLogger(
    val log: MutableList<String> = mutableListOf(),
    val indent: String = "  ",
    val prefixer: (NodeType) -> String = NodeType::prefix
) : TestEventListener {

    enum class NodeType(val prefix: String) {
        CONTEXT("▾ "),
        TEST_COMPLETE("✓ "),
        TEST_FAILED("X "),
        TEST_ABORTED("- "),
        TEST_SKIPPED("- ")
    }

    companion object {
        val noSymbols: (NodeType) -> String = { "" }
    }

    private var currentIndent = ""

    override fun <PF, F> contextOpened(context: Context<PF, F>, testDescriptor: TestDescriptor) {
        log(NodeType.CONTEXT, testDescriptor)
    }

    override fun <F> testStarting(fixture: F, testDescriptor: TestDescriptor) = Unit

    override fun <F> testComplete(fixture: F, testDescriptor: TestDescriptor) {
        log(NodeType.TEST_COMPLETE, testDescriptor)
    }

    override fun <F> testFailed(fixture: F, testDescriptor: TestDescriptor, t: Throwable) {
        log(NodeType.TEST_FAILED, testDescriptor)
    }

    override fun <F> testAborted(fixture: F, testDescriptor: TestDescriptor, t: TestAbortedException) {
        log(NodeType.TEST_ABORTED, testDescriptor)
    }

    override fun <F> testSkipped(fixture: F, testDescriptor: TestDescriptor, t: IncompleteExecutionException) {
        log(NodeType.TEST_SKIPPED, testDescriptor)
    }

    override fun <PF, F> contextClosed(context: Context<PF, F>) {
        currentIndent = currentIndent.substring(indent.length)
    }

    private fun log(nodeType: NodeType, testDescriptor: TestDescriptor) {
        log.add(currentIndent + prefixer(nodeType) + testDescriptor.name)
        if (nodeType == NodeType.CONTEXT) {
            currentIndent += indent
        }
    }
}