package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.internal.RootExecutor
import com.oneeyedmen.minutest.internal.TestExecutor
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.junit.runners.ParentRunner
import org.junit.runners.model.Statement
import org.opentest4j.TestAbortedException

class MinutestJUnit4Runner(type: Class<*>) : ParentRunner<RuntimeNode<Unit>>(type) {

    override fun getChildren(): List<RuntimeNode<Unit>> {
        val testInstance = (testClass.javaClass.newInstance() as? JUnit4Minutests)
            ?: error("${this::class.simpleName} should be applied to an instance of JUnit4Minutests")
        return listOf(testInstance.rootContextFromMethods())
    }

    override fun runChild(child: RuntimeNode<Unit>, notifier: RunNotifier) = child.run(RootExecutor, notifier)

    override fun describeChild(child: RuntimeNode<Unit>) = child.toDescription(RootExecutor)

    private fun <F> RuntimeNode<F>.run(executor: TestExecutor<F>, notifier: RunNotifier): Unit = when (this) {
        is RuntimeTest<F> -> this.run(executor, notifier)
        is RuntimeContext<F, *> -> this.run(executor, notifier)
    }

    private fun <F> RuntimeTest<F>.run(executor: TestExecutor<F>, notifier: RunNotifier) {
        // We want to rely on runLeaf to fire events for us, but ignoring a test is a special case. So we run the test
        // first - if it turns out to have been skipped we fire the event for that, otherwise we make a statement
        // for runChild to chew on.
        val thrown: Throwable? = exceptionIfAnyFromRunning(this, executor)
        if (thrown is TestAbortedException)
            notifier.fireTestIgnored(this.toDescription(executor))
        else
            runLeaf(thrown.asStatement(), this.toDescription(executor), notifier)
    }

    private fun <PF, F> RuntimeContext<PF, F>.run(executor: TestExecutor<PF>, notifier: RunNotifier) {
        children.forEach { child ->
            child.run(executor.andThen(this), notifier)
        }
        close()
// TODO - If we don't fire this event, IntelliJ gets confused and shows contexts still in progress.
// If we do fire it, then RunListenerAdapter fires it again, confusing other things.
//        notifier.fireTestFinished(toDescription(executor))
    }
}

private fun <F> RuntimeNode<F>.toDescription(executor: TestExecutor<F>): Description = when (this) {
    is RuntimeTest -> Description.createTestDescription(executor.name, this.name)
    is RuntimeContext<F, *> -> this.toDescription(executor)
}

private fun <PF, F> RuntimeContext<PF, F>.toDescription(executor: TestExecutor<PF>) =
    Description.createSuiteDescription(this.name).also { description ->
        this.children.forEach {
            description.addChild(it.toDescription(executor.andThen(this)))
        }
    }

private fun <F> exceptionIfAnyFromRunning(runtimeTest: RuntimeTest<F>, executor: TestExecutor<F>): Throwable? =
    try {
        executor.runTest(runtimeTest)
        null
    } catch (t: Throwable) {
        t
    }

private fun Throwable?.asStatement() = object : Statement() {
    override fun evaluate() {
        this@asStatement?.let { throw it }
    }
}