package com.oneeyedmen.minutest.internal

import com.oneeyedmen.minutest.Named
import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestDescriptor

interface ParentContext<F> : Named {
    fun runTest(test: Test<F>, testName: String)

    fun andThen(nextContext: RuntimeContext): ParentContext<Any?> = object: ParentContext<Any?> {
        override val name = nextContext.name
        override val parent = this@ParentContext
        override fun runTest(test: Test<Any?>, testName: String) {
            nextContext.runTest(test, this@ParentContext, testName)
        }

        override fun newRunTest(test: Test<Any?>, testDescriptor: TestDescriptor) {
            // NB use the top testDescriptor so that we always see the longest path - the one from the
            // bottom of the stack.
            val testForParent: Test<F> = { fixture: F, _: TestDescriptor ->
                nextContext.newRunTest(test, fixture as Any, testDescriptor) as F
            }
            return parent.newRunTest(testForParent, testDescriptor)
        }
    }

    fun newRunTest(test: Test<F>, testDescriptor: TestDescriptor)
}

internal object RootContext : ParentContext<Unit> {
    override val name = ""
    override val parent: Nothing? = null
    override fun runTest(test: Test<Unit>, testName: String) = test(Unit, this)

    override fun newRunTest(test: Test<Unit>, testDescriptor: TestDescriptor): Unit = test(Unit, testDescriptor)
}