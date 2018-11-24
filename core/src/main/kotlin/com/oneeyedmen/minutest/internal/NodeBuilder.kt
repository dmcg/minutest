package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.RuntimeNode

interface NodeBuilder<F> {
    fun buildNode(parent: ParentContext<F>): RuntimeNode
}