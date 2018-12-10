package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.junit.runners.ParentRunner
import org.junit.runners.model.Statement
import org.opentest4j.TestAbortedException

class MinutestJUnit4Runner(type: Class<*>) : ParentRunner<RuntimeNode>(type) {

    private lateinit var rootContext: RuntimeContext

    override fun getChildren(): List<RuntimeNode> {
        val testInstance = (testClass.javaClass.newInstance() as? JUnit4Minutests) ?:
            error("${this::class.simpleName} should be applied to an instance of JUnit4Minutests")
        rootContext = testInstance.rootContextFromMethods()
        return rootContext.children
    }

    override fun runChild(child: RuntimeNode, notifier: RunNotifier) = run(child, notifier)

    override fun describeChild(child: RuntimeNode) = child.toDescription()

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

    private fun run(node: RuntimeNode, notifier: RunNotifier) = when(node) {
        is RuntimeTest -> run(node, notifier)
        is RuntimeContext -> run(node, notifier)
    }

    private fun run(test: RuntimeTest, notifier: RunNotifier) {
        runLeaf(test.asStatement(notifier), test.toDescription(), notifier)
    }

    private fun run(context: RuntimeContext, notifier: RunNotifier) {
        notifier.fireTestStarted(context.toDescription())
        context.children.forEach {
            run(it, notifier)
        }
        context.close()
        notifier.fireTestFinished(context.toDescription())
    }
}

private fun RuntimeNode.toDescription(): Description = when (this) {
    is RuntimeTest -> Description.createTestDescription(parent?.name.orEmpty(), this.name)
    is RuntimeContext -> Description.createSuiteDescription(this.name).apply {
        this@toDescription.children.forEach {
            addChild(it.toDescription())
        }
    }
}

private fun RuntimeTest.asStatement(notifier: RunNotifier) = object : Statement() {
    override fun evaluate() {
        try {
            run()
        } catch (aborted: TestAbortedException) {
            // JUnit 4 doesn't understand JUnit 5's convention
            notifier.fireTestIgnored(this@asStatement.toDescription())
        }
    }
}