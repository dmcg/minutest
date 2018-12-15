package com.oneeyedmen.minutest

sealed class RuntimeNode : Named {
    abstract override val parent: RuntimeContext<*>?
    abstract val properties: Map<Any, Any>
    abstract fun withProperties(properties: Map<Any, Any>): RuntimeNode
}

abstract class RuntimeContext<F> : RuntimeNode(), AutoCloseable {
    abstract val children: List<RuntimeNode>
    abstract override fun withProperties(properties: Map<Any, Any>): RuntimeContext<F>
    abstract fun withChildren(children: List<RuntimeNode>): RuntimeContext<F>
    abstract fun runTest(test: Test<F>)
}

abstract class RuntimeTest: RuntimeNode() {
    abstract fun run()
    abstract override fun withProperties(properties: Map<Any, Any>): RuntimeTest
}

data class LoadedRuntimeContext<F>(
    override val name: String,
    override val parent: RuntimeContext<*>?,
    override val properties: Map<Any, Any>,
    override val children: List<RuntimeNode>,
    val runnner: (test: Test<F>)-> Unit,
    val onClose: () -> Unit
) : RuntimeContext<F>() {

    override fun runTest(test: Test<F>) = runnner(test)

    constructor(
        delegate: RuntimeContext<F>,
        name: String = delegate.name,
        parent: RuntimeContext<*>? = delegate.parent,
        properties: Map<Any, Any> = delegate.properties,
        children: List<RuntimeNode> = delegate.children,
        onClose: () -> Unit = delegate::close
        ) :
        this(name, parent, properties, children, { delegate.runTest(it) }, onClose)

    override fun withChildren(children: List<RuntimeNode>) = copy(children = children)
    override fun withProperties(properties: Map<Any, Any>) = copy(properties = properties)

    override fun close() {
        onClose.invoke()
    }
}