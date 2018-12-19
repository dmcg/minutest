package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.internal.ParentContext

sealed class RuntimeNode : Named {
    abstract override val name: String
    override val parent = null
    abstract val properties: Map<Any, Any>
    abstract fun withProperties(properties: Map<Any, Any>): RuntimeNode
}

abstract class RuntimeContext : RuntimeNode(), AutoCloseable {
    abstract val children: List<RuntimeNode>
    abstract fun withChildren(children: List<RuntimeNode>): RuntimeContext
    abstract fun runTest(test: Test<*>, parentContext: ParentContext<*>)
}

abstract class RuntimeTest: RuntimeNode() {
    abstract fun run(parentContext: ParentContext<*>)
}


fun <F> ParentContext<F>.andThen(nextContext: RuntimeContext): ParentContext<Any?> {
    return object: ParentContext<Any?> {
        override val name = nextContext.name
        override val parent = this@andThen
        override fun runTest(test: Test<Any?>) {
            nextContext.runTest(test, this@andThen)
        }
    }
}

data class LoadedRuntimeTest(
    override val name: String,
    override val properties: Map<Any, Any>,
    val xRunner: (ParentContext<*>) -> Unit
) : RuntimeTest() {

    constructor(
        delegate: RuntimeTest,
        name: String = delegate.name,
        properties: Map<Any, Any> = delegate.properties,
        xRunner: (ParentContext<*>) -> Unit = delegate::run
    ) :
        this(name, properties, xRunner)

    override fun withProperties(properties: Map<Any, Any>) = copy(properties = properties)

    override fun run(parentContext: ParentContext<*>) {
        xRunner(parentContext)
    }
}

data class LoadedRuntimeContext(
    override val name: String,
    override val properties: Map<Any, Any>,
    override val children: List<RuntimeNode>,
    val runner: (Test<*>, ParentContext<*>) -> Unit,
    val onClose: () -> Unit
) : RuntimeContext() {
    override fun runTest(test: Test<*>, parentContext: ParentContext<*>) = runner(test, parentContext)

    constructor(
        delegate: RuntimeContext,
        name: String = delegate.name,
        properties: Map<Any, Any> = delegate.properties,
        children: List<RuntimeNode> = delegate.children,
        runner: (Test<*>, ParentContext<*>) -> Unit = delegate::runTest,
        onClose: () -> Unit = delegate::close
        ) :
        this(name, properties, children, runner, onClose)

    override fun withChildren(children: List<RuntimeNode>) = copy(children = children)
    override fun withProperties(properties: Map<Any, Any>) = copy(properties = properties)

    override fun close() {
        onClose.invoke()
    }
}