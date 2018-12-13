package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.internal.ParentContext
import com.oneeyedmen.minutest.internal.RootContext

@Suppress("unused") // F is only there to show that the other type is the parent type and to keep the tree honest
interface NodeBuilder<ParentF, F> {
    val properties: MutableMap<Any, Any>
    fun buildNode(parent: ParentContext<ParentF>): RuntimeNode
}

/**
 * Marker interface so we know roots when we see them.
 */
interface RootNodeBuilder<F> : NodeBuilder<Unit, F>

fun NodeBuilder<Unit, *>.buildRootNode(): RuntimeNode = buildNode(RootContext)