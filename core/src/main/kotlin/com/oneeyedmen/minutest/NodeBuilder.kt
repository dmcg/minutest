package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.internal.ParentContext
import com.oneeyedmen.minutest.internal.RootContext

@Suppress("unused") // F is only there to show that the other type is the parent type
interface NodeBuilder<ParentF, F> {
    val properties: MutableMap<Any, Any>
    fun buildNode(parent: ParentContext<ParentF>): RuntimeNode
}

fun NodeBuilder<Unit, *>.buildRootNode(): RuntimeNode = buildNode(RootContext)