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

class MinutestJUnit4Runner(type: Class<*>) : ParentRunner<RuntimeNode<Unit, *>>(type) {

    private lateinit var rootContext: RuntimeContext<Unit, *>

    override fun getChildren(): List<RuntimeNode<Unit,*>> {
        val testInstance = (testClass.javaClass.newInstance() as? JUnit4Minutests) ?:
            error("${this::class.simpleName} should be applied to an instance of JUnit4Minutests")
        rootContext = testInstance.rootContextFromMethods()
        return rootContext.children as List<RuntimeNode<Unit, *>>
    }

    override fun runChild(child: RuntimeNode<Unit, *>, notifier: RunNotifier) = child.run(RootExecutor, notifier)

    override fun describeChild(child: RuntimeNode<Unit, *>) = child.toDescription(RootExecutor)

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

    private fun <PF, F> RuntimeNode<PF, F>.run(executor: TestExecutor<PF>, notifier: RunNotifier): Unit = when (this) {
        is RuntimeTest<*> -> run1(this as RuntimeTest<PF>, executor, notifier)
        is RuntimeContext -> run2(this, executor.andThen(this), notifier)
    }

//    private fun <PF, G> run(node: RuntimeNode<PF, G>, parentContext: ParentContext<PF>, notifier: RunNotifier) = when(node) {
//        is RuntimeTest<*> -> run1(node as RuntimeTest<G>, parentContext, notifier)
//        is RuntimeContext -> run2(node, parentContext.andThen(node), notifier)
//    }

    private fun <F> run1(test: RuntimeTest<F>, executor: TestExecutor<F>, notifier: RunNotifier) {
        runLeaf(test.asStatement(executor, notifier), test.toDescription(executor), notifier)
    }

    private fun <PF, F> run2(context: RuntimeContext<PF, F>, executor: TestExecutor<F>, notifier: RunNotifier) {
        notifier.fireTestStarted(context.toDescription(executor))
        context.children.forEach {
            it.run(executor, notifier)
        }
        context.close()
        notifier.fireTestFinished(context.toDescription(executor))
    }
}

private fun RuntimeNode<*, *>.toDescription(executor: TestExecutor<*>): Description = when (this) {
    is RuntimeTest -> Description.createTestDescription(executor.name, this.name)
    is RuntimeContext -> Description.createSuiteDescription(this.name).apply {
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