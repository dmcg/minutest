package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest


fun RuntimeNode<*>.hasA(predicate: (RuntimeNode<*>) -> Boolean): Boolean = when (this) {
    is RuntimeTest -> predicate(this)
    is RuntimeContext<*, *> -> hasA(predicate)
}

fun RuntimeContext<*, *>.hasA(predicate: (RuntimeNode<*>) -> Boolean): Boolean {
    return predicate(this) || children.find { it.hasA(predicate) } != null
}