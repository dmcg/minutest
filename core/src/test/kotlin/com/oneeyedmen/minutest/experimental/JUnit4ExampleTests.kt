package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.*
import com.oneeyedmen.minutest.internal.transformedTopLevelContext
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runner.notification.RunNotifier
import org.junit.runners.ParentRunner
import org.junit.runners.model.Statement


@RunWith(MinutestJUnit4Runner::class)
class JUnit4ExampleTests {

    val tests = context<Unit> {

        test("test") {
            println("here")
        }

        context("context") {
            test("test") {
                println("here")
            }
            test("test 2") {
//                fail("here")
            }
            context("another context") {
                test("test") {}
            }
            context("empty context") {
            }
            context("context whose name is wrong") {
                test("test") {}
            }

        }
    }
}

private inline fun <reified F> Any.context(
    noinline transform: (RuntimeNode) -> RuntimeNode = { it },
    noinline builder: Context<Unit, F>.() -> Unit
): NodeBuilder<Unit> = transformedTopLevelContext(javaClass.canonicalName, transform, builder)

class MinutestJUnit4Runner(type: Class<*>) : ParentRunner<RuntimeNode>(type) {

    override fun getChildren(): List<RuntimeNode> {
        return (JUnit4ExampleTests().tests.buildRootNode() as RuntimeContext).children
    }


    override fun runChild(child: RuntimeNode, notifier: RunNotifier) = run(child, notifier)


    override fun describeChild(child: RuntimeNode) = child.toDescription()

    private fun run(node: RuntimeNode, notifier: RunNotifier) = when(node) {
        is RuntimeTest -> run(node, notifier)
        is RuntimeContext -> run(node, notifier)
    }

    private fun run(test: RuntimeTest, notifier: RunNotifier) {
        runLeaf(test.asStatement(), test.toDescription(), notifier)
    }

    private fun run(context: RuntimeContext, notifier: RunNotifier) {
        context.children.forEach {
            run(it, notifier)
        }
    }
}

private fun RuntimeNode.toDescription(): Description = when (this) {
    is RuntimeTest -> Description.createTestDescription("", this.name, System.identityHashCode(this))
    is RuntimeContext -> Description.createSuiteDescription(this.name, System.identityHashCode(this)).apply {
        this@toDescription.children.forEach {
            addChild(it.toDescription())
        }
    }
}

private fun RuntimeTest.asStatement() = object : Statement() {
    override fun evaluate() {
        run()
    }
}