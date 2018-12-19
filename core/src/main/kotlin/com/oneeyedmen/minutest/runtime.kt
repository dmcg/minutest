package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.internal.ParentContext

sealed class RuntimeNode : Named {
    abstract val properties: Map<Any, Any>
    abstract fun withProperties(properties: Map<Any, Any>): RuntimeNode
}

abstract class RuntimeContext : RuntimeNode(), AutoCloseable {
    abstract val children: List<RuntimeNode>
    abstract fun withChildren(children: List<RuntimeNode>): RuntimeContext
    abstract fun runTestX(test: Test<*>, parentContext: ParentContext<*>)
}

abstract class RuntimeTest: RuntimeNode() {
    abstract fun run()
    abstract fun runX(parentContext: ParentContext<*>)
}


fun <F> ParentContext<F>.andThen(nextContext: RuntimeContext): ParentContext<Any?> {
    return object: ParentContext<Any?> {
        override val name = "thing"
        override val parent = this
        override fun runTest(test: Test<Any?>) {
            nextContext.runTestX(test, this@andThen)
        }
    }
}

data class LoadedRuntimeTest(
    override val name: String,
    override val parent: Named?,
    override val properties: Map<Any, Any>,
    val block: () -> Unit,
    val xRunner: (ParentContext<*>) -> Unit
) : RuntimeTest() {

    constructor(
        delegate: RuntimeTest,
        name: String = delegate.name,
        parent: Named? = delegate.parent,
        properties: Map<Any, Any> = delegate.properties,
        block: () -> Unit = delegate::run,
        xRunner: (ParentContext<*>) -> Unit = delegate::runX
    ) :
        this(name, parent, properties, block, xRunner)

    override fun withProperties(properties: Map<Any, Any>) = copy(properties = properties)

    override fun runX(parentContext: ParentContext<*>) {
        xRunner(parentContext)
    }

    override fun run() {
        block()
    }
}

data class LoadedRuntimeContext(
    override val name: String,
    override val parent: Named?,
    override val properties: Map<Any, Any>,
    override val children: List<RuntimeNode>,
    val runner: (Test<*>, ParentContext<*>) -> Unit,
    val onClose: () -> Unit
) : RuntimeContext() {
    override fun runTestX(test: Test<*>, parentContext: ParentContext<*>) = runner(test, parentContext)

    constructor(
        delegate: RuntimeContext,
        name: String = delegate.name,
        parent: Named? = delegate.parent,
        properties: Map<Any, Any> = delegate.properties,
        children: List<RuntimeNode> = delegate.children,
        runner: (Test<*>, ParentContext<*>) -> Unit = delegate::runTestX,
        onClose: () -> Unit = delegate::close
        ) :
        this(name, parent, properties, children, runner, onClose)

    override fun withChildren(children: List<RuntimeNode>) = copy(children = children)
    override fun withProperties(properties: Map<Any, Any>) = copy(properties = properties)

    override fun close() {
        onClose.invoke()
    }
}