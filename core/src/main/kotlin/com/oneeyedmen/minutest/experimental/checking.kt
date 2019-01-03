package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.internal.RuntimeContextWrapper

fun <F> checkedAgainst(
    logger: TestLogger = TestLogger(mutableListOf(), indent = "  ", prefixer = TestLogger.noSymbols),
    check: (List<String>) -> Unit
): (RuntimeNode<F>) -> RuntimeNode<F> = { node ->
    when (node) {
        is RuntimeContext<F, *> -> {
            val telling: (RuntimeNode<F>) -> RuntimeNode<F> = telling(logger)
            RuntimeContextWrapper(telling(node) as RuntimeContext<F, Any?>, onClose = { check(logger.log) })
        }
        else -> TODO("checking when root is just a test")
    }
}

fun <F> loggedTo(log: MutableList<String>): (RuntimeNode<F>) -> RuntimeNode<F> = { context ->
    telling<F>(TestLogger(log))(context)
}

fun List<String>.withTabsExpanded(spaces: Int) = this.map { it.replace("\t", " ".repeat(spaces)) }

