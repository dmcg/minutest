package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeNode


interface TopLevelContextTransform {

    fun applyTo(root: RuntimeNode<Unit>): RuntimeNode<Unit>

    fun then(next: (TopLevelContextTransform)): TopLevelContextTransform = object: TopLevelContextTransform {
        override fun applyTo(root: RuntimeNode<Unit>): RuntimeNode<Unit> =
            next.applyTo(this@TopLevelContextTransform.applyTo(root))
    }
}