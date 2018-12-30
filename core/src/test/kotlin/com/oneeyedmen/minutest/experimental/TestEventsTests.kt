package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.*
import com.oneeyedmen.minutest.internal.RuntimeContextWrapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail


class TestEventsTests {

    val log = mutableListOf<String>()

    val listener = object : TestEventListener {

        override fun <F> testComplete(fixture: F, testDescriptor: TestDescriptor) {
            log.add("Completed " + testDescriptor.fullName())
        }

        override fun <F> testFailed(fixture: F, testDescriptor: TestDescriptor, t: Throwable) {
            log.add("Failed " + testDescriptor.fullName())
        }

        override fun <F> testStarting(fixture: F, testDescriptor: TestDescriptor) {
            log.add("Starting " + testDescriptor.fullName())
        }

        override fun <PF, F> contextClosed(runtimeContext: RuntimeContext<PF, F>) {
            log.add("Closed " + runtimeContext.name)
        }
    }

    @Test fun firesEvents() {

        val tests = rootContext<Unit>(transform = telling(listener)) {
            test("top level") {}
            context("outer") {
                test("in outer") {}
                context("inner") {
                    test("in inner") {}
                    test("fails") {
                        fail("Deliberate")
                    }
                }
            }
        }
        executeTests(tests)
        assertLogged(log,
            "Starting [root, top level]",
            "Completed [root, top level]",
            "Starting [root, outer, in outer]",
            "Completed [root, outer, in outer]",
            "Starting [root, outer, inner, in inner]",
            "Completed [root, outer, inner, in inner]",
            "Starting [root, outer, inner, fails]",
            "Failed [root, outer, inner, fails]",
            "Closed inner",
            "Closed outer",
            "Closed root"
        )
    }
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

interface TestEventListener {
    fun <F> testStarting(fixture: F, testDescriptor: TestDescriptor)
    fun <PF, F> contextClosed(runtimeContext: RuntimeContext<PF, F>)
    fun <F> testComplete(fixture: F, testDescriptor: TestDescriptor)
    fun <F> testFailed(fixture: F, testDescriptor: TestDescriptor, t: Throwable)
}