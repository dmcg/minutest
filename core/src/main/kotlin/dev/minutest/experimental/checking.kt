package dev.minutest.experimental

import dev.minutest.Context
import dev.minutest.Node
import dev.minutest.NodeTransform
import dev.minutest.TestContextBuilder

fun <PF, F> TestContextBuilder<PF, F>.checkedAgainst(
    logger: TestLogger = defaultLogger(),
    check: (List<String>) -> Unit
) {
    annotateWith(TransformingAnnotation { node ->
        when (node) {
            is Context<PF, *> -> {
                val telling: (Context<PF, *>) -> Context<PF, *> = telling(logger)
                ContextWrapper(telling(node), onClose = { check(logger.log) })
            }
            else -> TODO("checking when root is just a test")
        }
    })
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
) = this.checkedAgainst(logger = logger, expected = expected.toList(), checker = checker)


private fun defaultLogger() = TestLogger(mutableListOf(), prefixer = TestLogger.noSymbols)

// Raw to keep dependency on JUnit to a minimum
private fun <F> defaultChecker(expected: F, actual: F) {
    assert(actual == expected) {
        """
            Test log checking failed
            Expected : $expected
            Actual   : $actual""".trimIndent()
    }
}

fun <PF, F> TestContextBuilder<PF, F>.logTo(
    log: MutableList<String>
) {
    annotateWith(RootAnnotation<PF>( { node -> telling<Unit, Node<Unit>>(TestLogger(log))(node) }))
}

fun <F> logTo(log: MutableList<String>): NodeTransform<F> = { node ->
    telling<F, Node<F>>(TestLogger(log))(node)
}

