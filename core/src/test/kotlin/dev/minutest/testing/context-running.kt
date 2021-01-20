package dev.minutest.testing

import dev.minutest.RootContextBuilder
import dev.minutest.junit.toTestFactory
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.opentest4j.MultipleFailuresError
import java.util.stream.Stream

fun runTests(root: RootContextBuilder): List<Throwable> =
    runTests(root.toTestFactory())

fun List<Throwable>.andFailIfTheyFail() {
    when {
        this.size == 1 -> throw this[0]
        this.isNotEmpty() -> throw MultipleFailuresError("tests failed", this)
    }
}

private fun runTests(
    nodes: Stream<out DynamicNode>,
    exceptions: MutableList<Throwable> = mutableListOf()
): List<Throwable> {
    nodes.forEachOrdered { dynamicNode ->
        when (dynamicNode) {
            is DynamicTest -> try {
                dynamicNode.executable.execute()
            } catch (x: Throwable) {
                exceptions.add(x)
            }
            is DynamicContainer -> runTests(dynamicNode.children, exceptions)
        }
    }
    nodes.close()
    return exceptions
}

