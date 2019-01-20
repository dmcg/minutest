package com.oneeyedmen.minutest

interface RuntimeNodeTransform {

    fun <F> applyTo(node: RuntimeNode<F>): RuntimeNode<F>

    fun then(next: (RuntimeNodeTransform)): RuntimeNodeTransform = object: RuntimeNodeTransform {
        override fun <F> applyTo(node: RuntimeNode<F>): RuntimeNode<F> =
            next.applyTo(this@RuntimeNodeTransform.applyTo(node))
    }
}