package com.oneeyedmen.minutest

sealed class RuntimeNode : Named {
    abstract val properties: Map<Any, Any>
}

abstract class RuntimeContext : RuntimeNode() {
    abstract val children: List<RuntimeNode>
    abstract fun withChildren(children: List<RuntimeNode>): RuntimeContext
}

abstract class RuntimeTest: RuntimeNode() {
    abstract fun run()
}