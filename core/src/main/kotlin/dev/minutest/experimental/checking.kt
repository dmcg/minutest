package dev.minutest.experimental

import dev.minutest.Context
import dev.minutest.Node

fun <F> checkedAgainst(
    logger: TestLogger = TestLogger(mutableListOf(), prefixer = TestLogger.noSymbols),
    check: (List<String>) -> Unit
): (Node<F>) -> Node<F> = { node ->
    when (node) {
        is Context<F, *> -> {
            val telling: (Context<F, *>) -> Context<F, *> = telling(logger)
            ContextWrapper(telling(node), onClose = { check(logger.log) })
        }
        else -> TODO("checking when root is just a test")
    }
}

fun <F> loggedTo(log: MutableList<String>): (Node<F>) -> Node<F> = { node ->
    telling<F, Node<F>>(TestLogger(log))(node)
}