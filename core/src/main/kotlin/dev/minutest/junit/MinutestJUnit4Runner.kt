package dev.minutest.junit

import dev.minutest.internal.*
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.junit.runners.ParentRunner
import org.junit.runners.model.Statement
import org.opentest4j.TestAbortedException

class MinutestJUnit4Runner(type: Class<*>) : ParentRunner<RunnableNode>(type) {

    override fun getChildren(): List<RunnableNode> =
        rootContextForClass(
            testClass.javaClass.kotlin
        )?.children?.map { it.toRootContext() } ?: error("Couldn't find any test methods")

    override fun runChild(child: RunnableNode, notifier: RunNotifier) =
        child.run(notifier)

    override fun describeChild(child: RunnableNode) = child.toDescription()

    private fun RunnableNode.run(notifier: RunNotifier): Unit =
        when (this) {
            is RunnableTest -> this.run(notifier)
            is RunnableContext -> this.run(notifier)
        }

    private fun RunnableTest.run(notifier: RunNotifier) {
        // We want to rely on runLeaf to fire events for us, but ignoring a test is a special case.
        // So we run the test first - if it turns out to have been skipped we fire the event for that,
        // otherwise we make a statement that will throw the exception when runLeaf chews on it.
        val thrown: Throwable? = exceptionIfAnyFromRunning(this)
        if (thrown is TestAbortedException)
            notifier.fireTestIgnored(this.toDescription())
        else
            runLeaf(thrown.asStatement(), this.toDescription(), notifier)
    }

    private fun RunnableContext.run(notifier: RunNotifier) {
        children.forEach { child ->
            child.run(notifier)
        }
// TODO - If we don't fire this event, IntelliJ gets confused and shows contexts still in progress.
// If we do fire it, then RunListenerAdapter fires it again, confusing other things.
//        notifier.fireTestFinished(toDescription(executor))
        this.close()
    }
}

private fun RunnableNode.toDescription(): Description = when (this) {
    is RunnableTest -> toDescription()
    is RunnableContext -> this.toDescription()
}

private fun RunnableTest.toDescription() =
    Description.createTestDescription(
        testDescriptor.name,
        this.name
    )

private fun RunnableContext.toDescription() =
    Description.createSuiteDescription(this.name).also { description ->
        this.children.forEach {
            description.addChild(it.toDescription())
        }
    }

private fun exceptionIfAnyFromRunning(test: RunnableTest): Throwable? =
    try {
        test.invoke()
        null
    } catch (t: Throwable) {
        t
    }

private fun Throwable?.asStatement() = object : Statement() {
    override fun evaluate() {
        this@asStatement?.let { throw it }
    }
}