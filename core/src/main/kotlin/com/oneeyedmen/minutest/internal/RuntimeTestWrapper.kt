package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.RuntimeTest

internal data class RuntimeTestWrapper(
    override val name: String,
    override val properties: Map<Any, Any>,
    val f: (ParentContext<*>) -> Unit
) : RuntimeTest() {

    constructor(
        delegate: RuntimeTest,
        name: String = delegate.name,
        properties: Map<Any, Any> = delegate.properties,
        f: (ParentContext<*>) -> Unit = delegate::run
    ) : this(name, properties, f)

    override fun run(parentContext: ParentContext<*>) = f(parentContext)
}