package com.oneeyedmen.minutest

sealed class RuntimeNode : Named {
    abstract val properties: Map<Any, Any>
    abstract fun withProperties(properties: Map<Any, Any>): RuntimeNode
}

abstract class RuntimeContext : RuntimeNode(), AutoCloseable {
    abstract val children: List<RuntimeNode>
    abstract fun withChildren(children: List<RuntimeNode>): RuntimeContext
    abstract fun runTest(test: Test<*>)
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

    constructor(
        delegate: RuntimeTest,
        name: String = delegate.name,
        parent: Named? = delegate.parent,
        properties: Map<Any, Any> = delegate.properties,
        block: () -> Unit = delegate::run
    ) :
        this(name, parent, properties, block)

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
    val runnner: (test: Test<*>)-> Unit,
    val onClose: () -> Unit
) : RuntimeContext() {

    override fun runTest(test: Test<*>) = runnner(test)

    constructor(
        delegate: RuntimeContext,
        name: String = delegate.name,
        parent: Named? = delegate.parent,
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