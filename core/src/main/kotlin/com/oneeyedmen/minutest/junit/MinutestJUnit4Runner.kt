package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.internal.ParentContext
import com.oneeyedmen.minutest.internal.RootContext
import com.oneeyedmen.minutest.internal.andThenJust
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

    override fun runChild(child: RuntimeNode<Unit, *>, notifier: RunNotifier) = child.run(RootContext, notifier)

    override fun describeChild(child: RuntimeNode<Unit, *>) = child.toDescription(RootContext)

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

    private fun <PF, F> RuntimeNode<PF, F>.run(parentContext: ParentContext<PF>, notifier: RunNotifier): Unit = when (this) {
        is RuntimeTest<*> -> run1(this as RuntimeTest<PF>, parentContext, notifier)
        is RuntimeContext -> run2(this, parentContext.andThen(this), notifier)
    }

//    private fun <PF, G> run(node: RuntimeNode<PF, G>, parentContext: ParentContext<PF>, notifier: RunNotifier) = when(node) {
//        is RuntimeTest<*> -> run1(node as RuntimeTest<G>, parentContext, notifier)
//        is RuntimeContext -> run2(node, parentContext.andThen(node), notifier)
//    }

    private fun <F> run1(test: RuntimeTest<F>, parentContext: ParentContext<F>, notifier: RunNotifier) {
        runLeaf(test.asStatement(parentContext, notifier), test.toDescription(parentContext), notifier)
    }

    private fun <PF, F> run2(context: RuntimeContext<PF, F>, parentContext: ParentContext<F>, notifier: RunNotifier) {
        notifier.fireTestStarted(context.toDescription(parentContext))
        context.children.forEach {
            it.run(parentContext, notifier)
        }
        context.close()
        notifier.fireTestFinished(context.toDescription(parentContext))
    }
}

private fun RuntimeNode<*, *>.toDescription(parentContext: ParentContext<*>): Description = when (this) {
    is RuntimeTest -> Description.createTestDescription(parentContext.name, this.name)
    is RuntimeContext -> Description.createSuiteDescription(this.name).apply {
        this@toDescription.children.forEach {
            addChild(it.toDescription(parentContext))
        }
    }
}

private fun <F> RuntimeTest<F>.asStatement(parentContext: ParentContext<F>, notifier: RunNotifier) = object : Statement() {
    override fun evaluate() {
        try {
            parentContext.newRunTest(this@asStatement, parentContext.andThenJust(this@asStatement.name))
        } catch (aborted: TestAbortedException) {
            // JUnit 4 doesn't understand JUnit 5's convention
            notifier.fireTestIgnored(this@asStatement.toDescription(parentContext))
        }
    }
}