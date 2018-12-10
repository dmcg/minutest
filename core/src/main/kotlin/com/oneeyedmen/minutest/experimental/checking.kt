package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.*

fun checkedAgainst(check: (List<String>) -> Unit): (RuntimeNode) -> RuntimeNode = { node ->
    when (node) {
        is RuntimeTest -> error("Can only check a context")
        is RuntimeContext -> {
            val log = mutableListOf<String>()
            loggingRuntimeContext(node, log, 0) {
                check(log)
            }
        }
    }
}

fun loggedTo(log: MutableList<String>): (RuntimeNode) -> RuntimeNode = { node ->
    node.loggedTo(log, 0)
}

fun List<String>.withTabsExpanded(spaces: Int) = this.map { it.replace("\t", " ".repeat(spaces)) }

private fun RuntimeNode.loggedTo(log: MutableList<String>, level: Int): RuntimeNode =
    when (this) {
        is RuntimeContext -> loggingRuntimeContext(this, log, level)
        is RuntimeTest -> loggingRuntimeTest(this, log, level)
    }

private fun loggingRuntimeContext(
    wrapped: RuntimeContext,
    log: MutableList<String>,
    indent: Int,
    done: () -> Unit = {}
): RuntimeContext {
    val childrenLog = mutableListOf<String>()
    return LoadedRuntimeContext(wrapped,
        children = wrapped.children.map { it.loggedTo(childrenLog, indent + 1) },
        onClose = {
            log.add("${indent.tabs()}${wrapped.name}")
            log.addAll(childrenLog)
            wrapped.close()
            done()
        }
    )
}


private fun loggingRuntimeTest(wrapped: RuntimeTest, log: MutableList<String>, indent: Int): RuntimeTest =
    LoadedRuntimeTest(wrapped, block = {
        log.add("${indent.tabs()}${wrapped.name}")
        wrapped.run()
    })

private fun Int.tabs() = "\t".repeat(this)