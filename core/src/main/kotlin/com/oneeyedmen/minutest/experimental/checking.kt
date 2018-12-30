package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.internal.RuntimeContextWrapper

fun <F> checkedAgainst(check: (List<String>) -> Unit): (RuntimeContext<Unit, F>) -> RuntimeContext<Unit, F> = { context ->
    val log = mutableListOf<String>()
    context.loggedTo(log, 0) {
        check(log)
    }
}

fun <PF, F> loggedTo(log: MutableList<String>): (RuntimeContext<PF, F>) -> RuntimeContext<PF, F> =
    { context: RuntimeContext<PF, F> ->
        context.loggedTo(log, 0)
    }

fun List<String>.withTabsExpanded(spaces: Int) = this.map { it.replace("\t", " ".repeat(spaces)) }

private fun <F> RuntimeNode<F>.loggedTo(log: MutableList<String>, level: Int): RuntimeNode<F> =
    when (this) {
        is RuntimeTest<F> -> this.loggedTo(log, level)
        is RuntimeContext<F, *> -> this.loggedTo(log, level)
    }

private fun <PF, F> RuntimeContext<PF, F>.loggedTo(
    log: MutableList<String>,
    indent: Int,
    done: () -> Unit = {}
): RuntimeContext<PF, F> {
    val childrenLog = mutableListOf<String>()
    return RuntimeContextWrapper(
        this,
        children = children.map { it.loggedTo(childrenLog, indent + 1) },
        onClose = {
            log.add("${indent.tabs()}$name")
            log.addAll(childrenLog)
            close()
            done()
        }
    )
}


private fun <F> RuntimeTest<F>.loggedTo(log: MutableList<String>, indent: Int) = copy(
    f = { fixture, testDescriptor ->
        log.add("${indent.tabs()}$name")
        this(fixture, testDescriptor)
    }
)

private fun Int.tabs() = "\t".repeat(this)

