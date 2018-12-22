package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.Test

internal data class RuntimeTestWrapper(
    override val name: String,
    override val properties: Map<Any, Any>,
    val f: Test<Any?>
) : RuntimeTest(), Test<Any?> by f {

    constructor(
        delegate: RuntimeTest,
        name: String = delegate.name,
        properties: Map<Any, Any> = delegate.properties,
        f: Test<Any?> = delegate
    ) : this(name, properties, f)

    override fun run(parentContext: ParentContext<*>) = error("removed")
}