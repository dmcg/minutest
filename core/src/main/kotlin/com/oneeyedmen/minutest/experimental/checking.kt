package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.Node
import com.oneeyedmen.minutest.internal.ContextWrapper

fun <F> checkedAgainst(
    logger: TestLogger = TestLogger(mutableListOf(), prefixer = TestLogger.noSymbols),
    check: (List<String>) -> Unit
): (Node<F>) -> Node<F> = { node ->
    when (node) {
        is Context<F, *> -> {
            val telling: (Node<F>) -> Node<F> = telling(logger)
            ContextWrapper(telling(node) as Context<F, Any?>, onClose = { check(logger.log) })
        }
        else -> TODO("checking when root is just a test")
    }
}

fun <F> loggedTo(log: MutableList<String>): (Node<F>) -> Node<F> = { node ->
    telling<F>(TestLogger(log))(node)
}