package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Named
import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestDescriptor

interface ParentContext<F> : Named {
    fun newRunTest(test: Test<F>, testDescriptor: TestDescriptor)

    fun andThen(nextContext: RuntimeContext): ParentContext<Any?> = object: ParentContext<Any?> {
        override val name = nextContext.name
        override val parent = this@ParentContext

        override fun newRunTest(test: Test<Any?>, testDescriptor: TestDescriptor) {
            // NB use the top testDescriptor so that we always see the longest path - the one from the
            // bottom of the stack.
            val testForParent: Test<F> = { fixture: F, _: TestDescriptor ->
                nextContext.runTest(test, fixture as Any, testDescriptor) as F
            }
            return parent.newRunTest(testForParent, testDescriptor)
        }
    }
}

internal object RootContext : ParentContext<Unit> {
    override val name = ""
    override val parent: Nothing? = null
    override fun newRunTest(test: Test<Unit>, testDescriptor: TestDescriptor): Unit = test(Unit, testDescriptor)
}