package com.oneeyedmen.minutest

interface NodeBuilder<PF, F> {
    val properties: MutableMap<Any, Any>
    fun buildNode(): RuntimeNode<PF, F>
}
