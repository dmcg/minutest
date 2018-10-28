package com.oneeyedmen.minutest.internal

internal interface Node {
    val name: String
    fun toRuntimeNode(): RuntimeNode
}