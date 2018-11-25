package com.oneeyedmen.minutest

sealed class RuntimeNode : Named {
    abstract val properties: Map<String, Any>
}

abstract class RuntimeContext : RuntimeNode() {
    abstract val children: List<RuntimeNode>
}

abstract class RuntimeTest: RuntimeNode() {
    abstract fun run()
}