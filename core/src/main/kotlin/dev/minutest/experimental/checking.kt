package dev.minutest.experimental

import dev.minutest.internal.ContextWrapper

fun <F> checkedAgainst(
    logger: TestLogger = TestLogger(mutableListOf(), prefixer = TestLogger.noSymbols),
    check: (List<String>) -> Unit
): (dev.minutest.Node<F>) -> dev.minutest.Node<F> = { node ->
    when (node) {
        is dev.minutest.Context<F, *> -> {
            val telling: (dev.minutest.Node<F>) -> dev.minutest.Node<F> = telling(logger)
            ContextWrapper(telling(node) as dev.minutest.Context<F, Any?>, onClose = { check(logger.log) })
        }
        else -> TODO("checking when root is just a test")
    }
}

fun <F> loggedTo(log: MutableList<String>): (dev.minutest.Node<F>) -> dev.minutest.Node<F> = { node ->
    telling<F>(TestLogger(log))(node)
}