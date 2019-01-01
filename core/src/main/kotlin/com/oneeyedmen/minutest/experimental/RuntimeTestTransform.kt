package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest


interface RuntimeTestTransform<F> {
    fun apply(test: RuntimeTest<F>): RuntimeTest<F>
    fun then(next: (RuntimeTestTransform<F>)): RuntimeTestTransform<F> = object: RuntimeTestTransform<F> {
        override fun apply(test: RuntimeTest<F>): RuntimeTest<F> = next.apply(this.apply(test))
    }
}

fun <F> RuntimeTest<F>.transformedBy(annotations: List<TestAnnotation>): RuntimeNode<F> {
    val transforms: List<RuntimeTestTransform<F>> = annotations.filterIsInstance<RuntimeTestTransform<F>>()
    return if (transforms.isEmpty())
        this
    else {
        transforms.reduce(RuntimeTestTransform<F>::then).apply(this)
    }
}
