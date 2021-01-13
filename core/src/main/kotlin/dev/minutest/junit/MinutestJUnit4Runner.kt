package dev.minutest.junit

import dev.minutest.internal.*
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.junit.runners.ParentRunner
import org.junit.runners.model.Statement
import org.opentest4j.TestAbortedException

class MinutestJUnit4Runner(type: Class<*>) : ParentRunner<RunnableNode<Unit>>(type) {

    override fun getChildren(): List<RunnableNode<Unit>> = listOf(
        testClass
            .onlyConstructor
            .newInstance()
            .rootContextFromMethods()
            .toRootRunnableNode()
    )

    override fun runChild(child: RunnableNode<Unit>, notifier: RunNotifier) =
        child.run(notifier)

    override fun describeChild(child: RunnableNode<Unit>) = child.toDescription()

    private fun <F> RunnableNode<F>.run(notifier: RunNotifier): Unit =
        when (this) {
            is RunnableTest<F> -> this.run(notifier)
            is RunnableContext<F, *> -> this.run(notifier)
        }

    private fun <F> RunnableTest<F>.run(notifier: RunNotifier) {
        // We want to rely on runLeaf to fire events for us, but ignoring a test is a special case.
        // So we run the test first - if it turns out to have been skipped we fire the event for that,
        // otherwise we make a statement that will throw the exception when runLeaf chews on it.
        val thrown: Throwable? = exceptionIfAnyFromRunning(this)
        if (thrown is TestAbortedException)
            notifier.fireTestIgnored(this.toDescription())
        else
            runLeaf(thrown.asStatement(), this.toDescription(), notifier)
    }

    private fun <PF, F> RunnableContext<PF, F>.run(notifier: RunNotifier) {
        children.forEach { child ->
            child.run(notifier)
        }
// TODO - If we don't fire this event, IntelliJ gets confused and shows contexts still in progress.
// If we do fire it, then RunListenerAdapter fires it again, confusing other things.
//        notifier.fireTestFinished(toDescription(executor))
    }
}

private fun <F> RunnableNode<F>.toDescription(): Description = when (this) {
    is RunnableTest<F> -> toDescription()
    is RunnableContext<F, *> -> this.toDescription()
}

private fun <F> RunnableTest<F>.toDescription() =
    Description.createTestDescription(
        testDescriptor.name,
        this.name
    )

private fun <PF, F> RunnableContext<PF, F>.toDescription() =
    Description.createSuiteDescription(this.name).also { description ->
        this.children.forEach {
            description.addChild(it.toDescription())
        }
    }

private fun <F> exceptionIfAnyFromRunning(test: RunnableTest<F>): Throwable? =
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