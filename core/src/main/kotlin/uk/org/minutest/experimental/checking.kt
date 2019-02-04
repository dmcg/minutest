package uk.org.minutest.experimental

import uk.org.minutest.internal.ContextWrapper

fun <F> checkedAgainst(
    logger: TestLogger = TestLogger(mutableListOf(), prefixer = TestLogger.noSymbols),
    check: (List<String>) -> Unit
): (uk.org.minutest.Node<F>) -> uk.org.minutest.Node<F> = { node ->
    when (node) {
        is uk.org.minutest.Context<F, *> -> {
            val telling: (uk.org.minutest.Node<F>) -> uk.org.minutest.Node<F> = telling(logger)
            ContextWrapper(telling(node) as uk.org.minutest.Context<F, Any?>, onClose = { check(logger.log) })
        }
        else -> TODO("checking when root is just a test")
    }
}

fun <F> loggedTo(log: MutableList<String>): (uk.org.minutest.Node<F>) -> uk.org.minutest.Node<F> = { node ->
    telling<F>(TestLogger(log))(node)
}