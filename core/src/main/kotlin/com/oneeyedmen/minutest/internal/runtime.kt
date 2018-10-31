package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Named

// A runtime representation of the test tree

sealed class RuntimeNode: Named

class RuntimeTest(override val name: String, val block: () -> Unit) : RuntimeNode()

class RuntimeContext(override val name: String, val children: Sequence<RuntimeNode>) : RuntimeNode()
