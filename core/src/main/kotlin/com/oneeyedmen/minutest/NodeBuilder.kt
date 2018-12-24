package com.oneeyedmen.minutest

interface NodeBuilder<F> {
    val properties: MutableMap<Any, Any>
    fun buildNode(): RuntimeNode<F>
}
