package com.oneeyedmen.minutest

sealed class RuntimeNode : Named {
    abstract val properties: Map<Any, Any>
    abstract fun withProperties(properties: Map<Any, Any>): RuntimeNode
}

abstract class RuntimeContext : RuntimeNode(), AutoCloseable {
    abstract val children: List<RuntimeNode>
    abstract fun withChildren(children: List<RuntimeNode>): RuntimeContext
}

abstract class RuntimeTest: RuntimeNode() {
    abstract fun run()
}

data class LoadedRuntimeTest(
    override val name: String,
    override val parent: Named?,
    override val properties: Map<Any, Any>,
    val block: () -> Unit
) : RuntimeTest() {
    override fun withProperties(properties: Map<Any, Any>) = copy(properties = properties)

    override fun run() {
        block()
    }
}

data class LoadedRuntimeContext(
    override val name: String,
    override val parent: Named?,
    override val properties: Map<Any, Any>,
    override val children: List<RuntimeNode>,
    val onClose: () -> Unit
) : RuntimeContext() {
    override fun withChildren(children: List<RuntimeNode>) = copy(children = children)
    override fun withProperties(properties: Map<Any, Any>) = copy(properties = properties)

    override fun close() {
        onClose.invoke()
    }
}