package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.RuntimeNode

interface NodeBuilder<F> {
    val properties: MutableMap<Any, Any>
    fun buildNode(parent: ParentContext<F>): RuntimeNode
}