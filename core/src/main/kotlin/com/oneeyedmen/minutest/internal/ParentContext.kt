package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Named
import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.Test

interface ParentContext<F> : Named {
    fun runTest(test: Test<F>)

    fun andThen(nextContext: RuntimeContext): ParentContext<Any?> = object: ParentContext<Any?> {
        override val name = nextContext.name
        override val parent = this@ParentContext
        override fun runTest(test: Test<Any?>) {
            nextContext.runTest(test, this@ParentContext)
        }
    }
}

internal object RootContext : ParentContext<Unit> {
    override val name = ""
    override val parent: Nothing? = null
    override fun runTest(test: Test<Unit>) = test(Unit, this)
}