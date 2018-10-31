package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Named

internal interface Node : Named {
    fun toRuntimeNode(): RuntimeNode
}