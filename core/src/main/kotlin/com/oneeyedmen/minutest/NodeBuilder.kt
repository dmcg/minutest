package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.internal.ParentContext
import com.oneeyedmen.minutest.internal.RootContext

interface NodeBuilder<F> {
    val properties: MutableMap<Any, Any>
    fun buildNode(parent: ParentContext<F>): RuntimeNode
}

fun NodeBuilder<Unit>.buildRootNode(): RuntimeNode = buildNode(RootContext)