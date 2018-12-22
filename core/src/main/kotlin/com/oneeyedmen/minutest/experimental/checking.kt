package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.internal.RuntimeContextWrapper

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
): RuntimeContext<PF, F> {
    val childrenLog = mutableListOf<String>()
    return RuntimeContextWrapper(
        wrapped,
        children = wrapped.children.map { it.loggedTo(childrenLog, indent + 1) },
        onClose = {
            log.add("${indent.tabs()}${wrapped.name}")
            log.addAll(childrenLog)
            wrapped.close()
            done()
        }
    )
}


private fun <F> loggingRuntimeTest(wrapped: RuntimeTest<F>, log: MutableList<String>, indent: Int) = wrapped.copy(
    f = { fixture, testDescriptor ->
        log.add("${indent.tabs()}${wrapped.name}")
        wrapped(fixture, testDescriptor)
    }
)

private fun Int.tabs() = "\t".repeat(this)