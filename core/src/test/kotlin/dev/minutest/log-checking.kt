package dev.minutest

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue


fun assertLogged(log: List<String>, vararg expected: String) {
    assertEquals(expected.toList().joinToString("\n"), log.joinToString("\n"))
}

fun assertNothingLogged(log: List<String>) {
    assertEquals(emptyList<String>(), log)
}

fun assertThereIsALogItem(log: List<String>, predicate: (String) -> Boolean) {
    assertTrue(log.any(predicate)) { log.joinToString("\n") }
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