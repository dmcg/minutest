package dev.minutest.experimental

import dev.minutest.Context
import dev.minutest.TestContextBuilder

fun <PF, F> TestContextBuilder<PF, F>.checkedAgainst(
    logger: TestLogger = defaultLogger(),
    check: (List<String>) -> Unit
) {
    addTransform { node ->
        when (node) {
            is Context<PF, *> -> {
                ContextWrapper(node.telling(logger), onClose = { check(logger.log) })
            }
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

fun defaultLogger() = TestLogger(mutableListOf())
fun noSymbolsLogger() = TestLogger(mutableListOf(), prefixer = TestLogger.noSymbols)

// Raw to keep dependency on JUnit to a minimum
private fun <F> defaultChecker(expected: F, actual: F) {
    assert(actual == expected) {
        """
            Test log checking failed
            Expected : $expected
            Actual   : $actual""".trimIndent()
    }
}

fun <PF, F> TestContextBuilder<PF, F>.logTo(log: MutableList<String>) {
    annotateWith(RootAnnotation { node -> node.telling(TestLogger(log)) })
}
