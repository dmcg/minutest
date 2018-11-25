package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.RuntimeNode

interface NodeBuilder<F> {
    val properties: MutableMap<String, Any>
    fun buildNode(parent: ParentContext<F>): RuntimeNode
}