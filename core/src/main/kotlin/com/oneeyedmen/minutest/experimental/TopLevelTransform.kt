package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeNode


interface TopLevelTransform {

    fun applyTo(node: RuntimeNode<Unit>): RuntimeNode<Unit>

    fun then(next: (TopLevelTransform)): TopLevelTransform = object: TopLevelTransform {
        override fun applyTo(node: RuntimeNode<Unit>): RuntimeNode<Unit> =
            next.applyTo(this@TopLevelTransform.applyTo(node))
    }
}