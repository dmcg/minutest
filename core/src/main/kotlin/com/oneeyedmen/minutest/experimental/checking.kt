package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.internal.RuntimeContextWrapper

fun <F> checkedAgainst(check: (List<String>) -> Unit): (RuntimeContext<Unit, F>) -> RuntimeContext<Unit, F> = { context ->
    val log = mutableListOf<String>()
    RuntimeContextWrapper(telling<F>(TestLogger(log))(context), onClose = { check(log) })
}

fun <F> loggedTo(log: MutableList<String>): (RuntimeContext<Unit, F>) -> RuntimeContext<Unit, F> = { context ->
    telling<F>(TestLogger(log))(context)
}

fun List<String>.withTabsExpanded(spaces: Int) = this.map { it.replace("\t", " ".repeat(spaces)) }

