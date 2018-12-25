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

class MinutestJUnit4Runner(type: Class<*>) : ParentRunner<RuntimeContext<Unit, *>>(type) {

    override fun getChildren(): List<RuntimeContext<Unit, *>> {
        val testInstance = (testClass.javaClass.newInstance() as? JUnit4Minutests) ?:
            error("${this::class.simpleName} should be applied to an instance of JUnit4Minutests")
        return listOf(testInstance.rootContextFromMethods())
    }

    override fun runChild(child: RuntimeContext<Unit, *>, notifier: RunNotifier) = child.run(RootExecutor, notifier)

    override fun describeChild(child: RuntimeContext<Unit, *>) = child.toDescription(RootExecutor)

    private fun <F> RuntimeNode<F>.run(executor: TestExecutor<F>, notifier: RunNotifier): Unit = when (this) {
        is RuntimeTest<F> -> {
            runLeaf(this.asStatement(executor, notifier), this.toDescription(executor), notifier)
        }
        is RuntimeContext<F, *> -> this.run(executor, notifier)
    }

    private fun <PF, F> RuntimeContext<PF, F>.run(executor: TestExecutor<PF>, notifier: RunNotifier) {
        notifier.fireTestStarted(toDescription(executor))
        children.forEach {
            it.run(executor.andThen(this), notifier)
        }
        close()
        notifier.fireTestFinished(toDescription(executor))
    }
}

private fun RuntimeNode<*>.toDescription(executor: TestExecutor<*>): Description = when (this) {
    is RuntimeTest -> Description.createTestDescription(executor.name, this.name)
    is RuntimeContext<*, *> -> Description.createSuiteDescription(this.name).apply {
        this@toDescription.children.forEach {
            addChild(it.toDescription(executor))
        }
    }
}

private fun <F> RuntimeTest<F>.asStatement(executor: TestExecutor<F>, notifier: RunNotifier) = object : Statement() {
    override fun evaluate() {
        try {
            executor.runTest(this@asStatement)
        } catch (aborted: TestAbortedException) {
            // JUnit 4 doesn't understand JUnit 5's convention
            notifier.fireTestIgnored(this@asStatement.toDescription(executor))
        }
    }
}