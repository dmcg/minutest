package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.TestDescriptor
import com.oneeyedmen.minutest.internal.RuntimeContextWrapper


interface TestEventListener {
    fun <F> testStarting(fixture: F, testDescriptor: TestDescriptor)
    fun <F> testComplete(fixture: F, testDescriptor: TestDescriptor)
    fun <F> testFailed(fixture: F, testDescriptor: TestDescriptor, t: Throwable)
    fun <PF, F> contextClosed(runtimeContext: RuntimeContext<PF, F>)
}

fun <F> telling(listener: TestEventListener): (RuntimeContext<Unit, F>) -> RuntimeContext<Unit, F> = { context ->
    context.telling(listener)
}

private fun <PF, F> RuntimeContext<PF, F>.telling(listener: TestEventListener): RuntimeContext<PF, F> =
    RuntimeContextWrapper(this,
        children = children.map { it.telling(listener) },
        onClose = { listener.contextClosed(this@telling) }
    )

private fun <F> RuntimeTest<F>.telling(listener: TestEventListener) = copy(
    f = { fixture, testDescriptor ->
        listener.testStarting(fixture, testDescriptor)
        try {
            this(fixture, testDescriptor).also {
                listener.testComplete(fixture, testDescriptor)
            }
        } catch (t: Throwable) {
            listener.testFailed(fixture, testDescriptor, t)
            throw t
        }
    }
)

private fun <F> RuntimeNode<F>.telling(listener: TestEventListener): RuntimeNode<F> =
    when (this) {
        is RuntimeTest<F> -> this.telling(listener)
        is RuntimeContext<F, *> -> this.telling(listener)
    }

