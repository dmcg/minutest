package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeNode


interface RuntimeNodeTransform {

    fun <F> applyTo(node: RuntimeNode<F>): RuntimeNode<F>

    fun then(next: (RuntimeNodeTransform)): RuntimeNodeTransform = object: RuntimeNodeTransform {
        override fun <F> applyTo(node: RuntimeNode<F>): RuntimeNode<F> =
            next.applyTo(this@RuntimeNodeTransform.applyTo(node))
    }
}

fun <F> RuntimeNode<F>.transformedBy(annotations: List<TestAnnotation>): RuntimeNode<F> {
    val transforms: List<RuntimeNodeTransform> = annotations.filterIsInstance<RuntimeNodeTransform>()
    return if (transforms.isEmpty())
        this
    else {
        transforms.reduce(RuntimeNodeTransform::then).applyTo(this)
    }
}
