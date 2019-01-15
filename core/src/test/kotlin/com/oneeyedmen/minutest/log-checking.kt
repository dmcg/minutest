package com.oneeyedmen.minutest

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals


fun assertLogged(log: List<String>, vararg expected: String) {
    assertEquals(expected.toList(), log) { log.joinToString("\n") }
}

fun assertNothingLogged(log: List<String>) {
    Assertions.assertEquals(emptyList<String>(), log)
}

fun assertThereIsALogItem(log: List<String>, predicate: (String) -> Boolean) {
    Assertions.assertTrue(log.any(predicate)) { log.joinToString("\n") }
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
        assertEquals(emptyList<String>(), failures)
    }