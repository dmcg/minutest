package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeContext


interface TopLevelContextTransform<F> {
    fun applyTo(context: RuntimeContext<Unit, F>): RuntimeContext<Unit, F>
    fun then(next: (TopLevelContextTransform<F>)): TopLevelContextTransform<F> = object: TopLevelContextTransform<F> {
        override fun applyTo(context: RuntimeContext<Unit, F>): RuntimeContext<Unit, F> =
            next.applyTo(this@TopLevelContextTransform.applyTo(context))
    }
}