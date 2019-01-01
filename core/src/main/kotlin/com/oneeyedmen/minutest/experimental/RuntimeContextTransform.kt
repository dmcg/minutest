package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeContext


interface RuntimeContextTransform<PF, F> {
    fun applyTo(context: RuntimeContext<PF, F>): RuntimeContext<PF, F>
    fun then(next: (RuntimeContextTransform<PF, F>)): RuntimeContextTransform<PF, F> = object: RuntimeContextTransform<PF, F> {
        override fun applyTo(context: RuntimeContext<PF, F>): RuntimeContext<PF, F> =
            next.applyTo(this@RuntimeContextTransform.applyTo(context))
    }
}

fun <PF, F> RuntimeContext<PF, F>.transformedBy(annotations: List<TestAnnotation>): RuntimeContext<PF, F> {
    val transforms: List<RuntimeContextTransform<PF, F>> = annotations.filterIsInstance<RuntimeContextTransform<PF, F>>()
    return if (transforms.isEmpty())
        this
    else {
        transforms.reduce(RuntimeContextTransform<PF, F>::then).applyTo(this)
    }
}