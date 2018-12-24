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

class MinutestJUnit4Runner(type: Class<*>) : ParentRunner<RuntimeNode<*>>(type) {

    private lateinit var rootContext: RuntimeContext<Unit, *>

    override fun getChildren(): List<RuntimeNode<*>> {
        val testInstance = (testClass.javaClass.newInstance() as? JUnit4Minutests) ?:
            error("${this::class.simpleName} should be applied to an instance of JUnit4Minutests")
        rootContext = testInstance.rootContextFromMethods()
        return rootContext.children
    }

    override fun runChild(child: RuntimeNode<*>, notifier: RunNotifier) = (child as RuntimeNode<Unit>).run(RootExecutor, notifier)

    override fun describeChild(child: RuntimeNode<*>) = child.toDescription(RootExecutor)

    override fun classBlock(notifier: RunNotifier): Statement {
        // This is the only way that I've found to close the top level context
        val normal = super.classBlock(notifier)
        return object : Statement() {
            override fun evaluate() {
                normal.evaluate()
                rootContext.close()
            }
        }
    }

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