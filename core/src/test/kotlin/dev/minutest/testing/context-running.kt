package dev.minutest.testing

import dev.minutest.RootContextBuilder
import dev.minutest.junit.toTestFactory
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import java.util.stream.Stream

fun runTests(root: RootContextBuilder): List<Throwable> =
    runTests(root.toTestFactory())

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

