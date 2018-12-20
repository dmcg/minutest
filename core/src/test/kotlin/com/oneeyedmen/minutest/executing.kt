package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.internal.TopLevelContextBuilder
import com.oneeyedmen.minutest.junit.toTestFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.opentest4j.TestAbortedException
import java.util.stream.Stream

fun executeTests(tests: Stream<out DynamicNode>) {
    tests.use {
        it.forEachOrdered { dynamicNode ->
            when (dynamicNode) {
                is DynamicTest -> try { dynamicNode.executable.execute() } catch (x: TestAbortedException) {}
                is DynamicContainer -> executeTests(dynamicNode.children)
            }
        }
    }
}

fun executeTests(root: TopLevelContextBuilder<*>) = executeTests(root.toTestFactory())

fun assertLogged(log: List<String>, vararg expected: String) {
    assertEquals(expected.toList(), log)
}

fun assertNothingLogged(log: List<String>) {
    assertEquals(emptyList<String>(), log)
}
