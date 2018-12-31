package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.experimental.TestAnnotation

interface NodeBuilder<F> {
    val annotations: MutableList<TestAnnotation>
    fun buildNode(): RuntimeNode<F>
}
