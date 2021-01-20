package dev.minutest

import dev.minutest.testing.runTests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.*

@Suppress("UNCHECKED_CAST")
fun <T, L: List<T>> L.synchronized(): L = Collections.synchronizedList(this) as L

fun assertLogged(log: List<String>, vararg expected: String) {
    assertEquals(
        expected.toList().joinToString("\n"),
        log.joinToString("\n")
    )
}

fun assertLoggedInAnyOrder(log: List<String>, vararg expected: String) {
    assertEquals(
        expected.sorted().joinToString("\n"),
        log.sorted().joinToString("\n")
    )
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

fun check(
    expectedLog: List<String>,
    contextBuilder: (MutableList<String>) -> RootContextBuilder
): List<Throwable> {
    val log = mutableListOf<String>().synchronized()
    val exceptions = runTests(contextBuilder(log))
    assertEquals(
        expectedLog.joinToString("\n"),
        log.joinToString("\n")
    )
    return exceptions
}

fun List<Throwable>.withNoExceptions() {
    assertEquals(emptyList<Throwable>(), this)
}

fun List<Throwable>.withExceptionsMatching(
    vararg exceptionMatchers: (Throwable) -> Boolean
) {
    checkItems(this, *exceptionMatchers)
}