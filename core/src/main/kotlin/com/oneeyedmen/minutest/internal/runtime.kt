package com.oneeyedmen.minutest.internal

// A runtime representation of the test tree

sealed class RuntimeNode

class RuntimeTest(val name: String, val block: () -> Unit) : RuntimeNode()

class RuntimeContext(val name: String, val children: Sequence<RuntimeNode>) : RuntimeNode()
