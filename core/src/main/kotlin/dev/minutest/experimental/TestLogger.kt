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
        CONTEXT("▾ "), TEST_COMPLETE("✓ "), TEST_FAILED("X "), TEST_ABORTED("- "), TEST_SKIPPED("- ")
    }

    companion object {
        val noSymbols: (NodeType) -> String = { "" }
    }

    private var lastPath = emptyList<String>()

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

    override fun <PF, F> contextClosed(context: Context<PF, F>) = Unit

    private fun log(nodeType: NodeType, testDescriptor: TestDescriptor) {
        val path = testDescriptor.fullName()
        val commonPrefix = lastPath.commonPrefix(path)
        if (path == lastPath) {
            // copes with repeated tests with the same name
            log.add(indent.repeat(testDescriptor.fullName().size - 1) + prefixer(nodeType) + testDescriptor.name)
        }
        else
            path.subList(commonPrefix.size, path.size).forEachIndexed { i, name ->
                val isContext = name != testDescriptor.name
                val icon = prefixer(if (isContext) NodeType.CONTEXT else nodeType)
                log.add(indent.repeat(commonPrefix.size + i) + icon + name)
            }
        lastPath = path
    }
}

private fun <E> List<E>.commonPrefix(that: List<E>): List<E> =
    this.zip(that).takeWhile { (a, b) -> a == b }.map { (a, _) -> a }