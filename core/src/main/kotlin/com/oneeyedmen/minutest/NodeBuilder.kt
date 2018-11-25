package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.internal.ParentContext

interface NodeBuilder<F> {
    val properties: MutableMap<Any, Any>
    fun buildNode(parent: ParentContext<F>): RuntimeNode
}