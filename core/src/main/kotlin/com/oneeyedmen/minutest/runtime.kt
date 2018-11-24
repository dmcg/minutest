package com.oneeyedmen.minutest

sealed class RuntimeNode : Named

abstract class RuntimeContext : RuntimeNode() {
    abstract val children: List<RuntimeNode>
    abstract val properties: Map<String, Any>
}

abstract class RuntimeTest: RuntimeNode() {
    abstract fun run()
}