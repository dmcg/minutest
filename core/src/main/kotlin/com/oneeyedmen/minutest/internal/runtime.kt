package com.oneeyedmen.minutest.internal

// A runtime representation of the test tree

sealed class RuntimeNode {
    abstract val name: String
}

class RuntimeTest(override val name: String, val block: () -> Unit) : RuntimeNode()

class RuntimeContext(override val name: String, val children: Sequence<RuntimeNode>) : RuntimeNode()
