package dev.minutest.experimental

import dev.minutest.Context
import dev.minutest.TestContextBuilder

fun <PF, F> TestContextBuilder<PF, F>.checkedAgainst(
    logger: TestLogger = defaultLogger(),
    check: (List<String>) -> Unit
) {
    annotateWith(
        RootAnnotation { node ->
            node.telling(logger)
        }
    )

    addTransform { node ->
        when (node) {
            is Context<PF, *> ->
                ContextWrapper(
                    node,
                    onClose = {
                        check(logger.toStrings(node))
                    }
                )
            else -> TODO("checking when root is just a test")
        }
    }
}

fun <PF, F> TestContextBuilder<PF, F>.checkedAgainst(
    expected: List<String>,
    logger: TestLogger = defaultLogger(),
    checker: (List<String>, List<String>) -> Unit = ::defaultChecker
) = this.checkedAgainst(logger) { log ->
    checker(expected, log)
}

fun <PF, F> TestContextBuilder<PF, F>.checkedAgainst(
    vararg expected: String,
    logger: TestLogger = defaultLogger(),
    checker: (List<String>, List<String>) -> Unit = ::defaultChecker
) = this.checkedAgainst(expected.toList(), logger, checker)

/**
 * Convenience alias for checkedAgainst.
 */
fun <PF, F> TestContextBuilder<PF, F>.willRun(
    vararg expected: String,
    logger: TestLogger = defaultLogger(),
    checker: (List<String>, List<String>) -> Unit = ::defaultChecker
) = this.checkedAgainst(expected.toList(), logger, checker)

fun defaultLogger() = TestLogger()
fun noSymbolsLogger() = TestLogger(prefixer = TestLogger.noSymbols)

// Raw to keep dependency on JUnit to a minimum
private fun <F> assertEquals(expected: F, actual: F) {
    assert(actual == expected) {
        """
            Test log checking failed
            Expected : $expected
            Actual   : $actual""".trimIndent()
    }
}

fun defaultChecker(expected: List<String>, actual:List<String>) {
//    assertEquals(expected, actual)
    aysncChecker(expected, actual)
}

fun aysncChecker(expected: List<String>, actual:List<String>) {
    assertEquals(
        expected.map(String::trim).toSet(),
        actual.map(String::trim).toSet()
    )
}

fun <PF, F> TestContextBuilder<PF, F>.logTo(logger: TestLogger) {
    annotateWith(RootAnnotation { node -> node.telling(logger) })
}
