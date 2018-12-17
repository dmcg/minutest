package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.internal.RuntimeContextWrapper
import com.oneeyedmen.minutest.internal.RuntimeTestWrapper

fun <F> checkedAgainst(check: (List<String>) -> Unit): (RuntimeContext<Unit, F>) -> RuntimeContext<Unit, F> = { node ->
    val log = mutableListOf<String>()
    loggingRuntimeContext(node, log, 0) {
        check(log)
    }
}

fun <PF, F> loggedTo(log: MutableList<String>): (RuntimeContext<PF, F>) -> RuntimeContext<PF, F> =
    { context: RuntimeContext<PF, F> ->
        loggingRuntimeContext(context, log, 0)
    }

fun List<String>.withTabsExpanded(spaces: Int) = this.map { it.replace("\t", " ".repeat(spaces)) }

private fun <PF, F> RuntimeNode<PF, F>.loggedTo(log: MutableList<String>, level: Int): RuntimeNode<PF, F> =
    when (this) {
        is RuntimeContext<*, *> -> loggingRuntimeContext(this as RuntimeContext<PF, F>, log, level)
        is RuntimeTest<*> -> loggingRuntimeTest(this as RuntimeTest<F>, log, level) as RuntimeNode<PF, F>
    }

private fun <PF, F> loggingRuntimeContext(
    wrapped: RuntimeContext<PF, F>,
    log: MutableList<String>,
    indent: Int,
    done: () -> Unit = {}
) = LoggingRuntimeContextWrapper(wrapped, log, indent, done)


private fun <F> loggingRuntimeTest(wrapped: RuntimeTest<F>, log: MutableList<String>, indent: Int) =
    RuntimeTestWrapper(wrapped, block = { delegate ->
        log.add("${indent.tabs()}${wrapped.name}")
        delegate.run()
    })

private fun Int.tabs() = "\t".repeat(this)

private class LoggingRuntimeContextWrapper<PF, F>(
    wrapped: RuntimeContext<PF, F>,
    private val log: MutableList<String>,
    private val indent: Int,
    private val onDone: () -> Unit = {},
    private val childLog: MutableList<String> = mutableListOf()
) : RuntimeContextWrapper<PF, F>(wrapped, wrapped.children.map { it.loggedTo(childLog, indent + 1) }) {
    override fun close() {
        log.add("${indent.tabs()}${wrapped.name}")
        log.addAll(childLog)
        wrapped.close()
        onDone()
    }
}