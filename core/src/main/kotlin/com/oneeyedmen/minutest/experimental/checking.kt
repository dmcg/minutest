package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.internal.RuntimeContextWrapper
import com.oneeyedmen.minutest.internal.RuntimeTestWrapper

fun <F> checkedAgainst(check: (List<String>) -> Unit): (RuntimeContext<F>) -> RuntimeContext<F> = { node ->
    val log = mutableListOf<String>()
    loggingRuntimeContext(node, log, 0) {
        check(log)
    }
}

fun <F> loggedTo(log: MutableList<String>): (RuntimeContext<F>) -> RuntimeContext<F> = { context: RuntimeContext<F> ->
    loggingRuntimeContext(context, log, 0)
}

fun List<String>.withTabsExpanded(spaces: Int) = this.map { it.replace("\t", " ".repeat(spaces)) }

private fun RuntimeNode.loggedTo(log: MutableList<String>, level: Int): RuntimeNode =
    when (this) {
        is RuntimeContext<*> -> loggingRuntimeContext(this, log, level)
        is RuntimeTest -> loggingRuntimeTest(this, log, level)
    }

private fun <F> loggingRuntimeContext(
    wrapped: RuntimeContext<F>,
    log: MutableList<String>,
    indent: Int,
    done: () -> Unit = {}
) = LoggingRuntimeContextWrapper(wrapped, log, indent, done)


private fun loggingRuntimeTest(wrapped: RuntimeTest, log: MutableList<String>, indent: Int) =
    RuntimeTestWrapper(wrapped, block = { delegate ->
        log.add("${indent.tabs()}${wrapped.name}")
        delegate.run()
    })

private fun Int.tabs() = "\t".repeat(this)

private class LoggingRuntimeContextWrapper<F>(
    wrapped: RuntimeContext<F>,
    private val log: MutableList<String>,
    private val indent: Int,
    private val onDone: () -> Unit = {},
    private val childLog: MutableList<String> = mutableListOf()
) : RuntimeContextWrapper<F>(wrapped, wrapped.children.map { it.loggedTo(childLog, indent + 1) }) {
    override fun close() {
        log.add("${indent.tabs()}${wrapped.name}")
        log.addAll(childLog)
        wrapped.close()
        onDone()
    }
}