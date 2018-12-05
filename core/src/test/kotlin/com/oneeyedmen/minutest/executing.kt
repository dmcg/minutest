package com.oneeyedmen.minutest

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

fun assertLogged(log: List<String>, vararg expected: String) {
    assertEquals(expected.toList(), log)
}

fun assertNothingLogged(log: List<String>) {
    assertEquals(emptyList<String>(), log)
}
