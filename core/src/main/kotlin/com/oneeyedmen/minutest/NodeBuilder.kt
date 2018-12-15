package com.oneeyedmen.minutest

@Suppress("unused") // F is only there to show that the other type is the parent type
interface NodeBuilder<ParentF, F> {
    val properties: MutableMap<Any, Any>
    fun buildNode(parent: RuntimeContext?): RuntimeNode
}

fun NodeBuilder<Unit, *>.buildRootNode(): RuntimeNode = buildNode(null)