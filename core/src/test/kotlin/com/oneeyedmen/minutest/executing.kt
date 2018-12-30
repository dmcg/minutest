package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.internal.TopLevelContextBuilder
import com.oneeyedmen.minutest.junit.toTestFactory
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import java.util.stream.Stream
import kotlin.test.assertEquals

fun executeTests(tests: Stream<out DynamicNode>, exceptions: MutableList<Throwable> = mutableListOf()): List<Throwable> {
    tests.use {
        it.forEachOrdered { dynamicNode ->
            when (dynamicNode) {
                is DynamicTest -> try {
                    dynamicNode.executable.execute()
                } catch (x: Throwable) {
                    exceptions.add(x)
                }
                is DynamicContainer -> executeTests(dynamicNode.children, exceptions)
            }
        }
    }
    return exceptions
}

fun executeTests(root: TopLevelContextBuilder<*>) = executeTests(root.toTestFactory())

fun assertLogged(log: List<String>, vararg expected: String) {
    assertEquals(expected.toList(), log)
}

fun assertNothingLogged(log: List<String>) {
    assertEquals(emptyList(), log)
}

fun <T> checkItems(items: Collection<T>, vararg predicates: (T) -> Boolean) = items
    .also {
        assertEquals(predicates.size, items.size, "Collection not the same size as expected")
    }
    .zip(predicates.asList())
    .map { (item, predicate) -> item to predicate(item)}
    .filterNot { it.second }
    .map { it.first }
    .let { failures: List<T> ->
        assertEquals(emptyList(), failures)
    }