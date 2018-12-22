
package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.internal.ParentContext
import com.oneeyedmen.minutest.internal.RootContext
import com.oneeyedmen.minutest.internal.TopLevelContextBuilder
import com.oneeyedmen.minutest.internal.andThenJust
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.util.stream.Stream
import kotlin.streams.asStream

interface JUnit5Minutests {

    val tests: TopLevelContextBuilder<*>? get() = null // a clue to what to override

    /**
     * Provided so that JUnit will run the tests
     */
    @TestFactory
    fun tests(): Stream<out DynamicNode> = tests.let { testsFromVal ->
        when  {
            testsFromVal != null -> testsFromVal.buildNode().toStreamOfDynamicNodes(RootContext)
            else -> this.rootContextFromMethods().toStreamOfDynamicNodes(RootContext)
        }
    }
}

/**
 * Convert a root context into a JUnit 5 [@org.junit.jupiter.api.TestFactory].
 *
 * @see [NodeBuilder<Unit>#testFactory()]
 */
fun <F> testFactoryFor(root: TopLevelContextBuilder<F>) = root.buildNode().toStreamOfDynamicNodes(RootContext)

/**
 * Convert a root context into a JUnit 5 [@org.junit.jupiter.api.TestFactory]
 *
 * @see [testFactoryFor(NodeBuilder<Unit>)]
 */
fun <F> TopLevelContextBuilder<F>.toTestFactory() = testFactoryFor(this)

// These are defined as extensions to avoid taking a dependency on JUnit in the main package

private fun <PF, F> RuntimeContext<PF, F>.toStreamOfDynamicNodes(parentContext: ParentContext<PF>): Stream<out DynamicNode> =
    children.toStreamOfDynamicNodes(this, parentContext.andThen(this))

private fun <F> Iterable<RuntimeNode<F, *>>.toStreamOfDynamicNodes(parent: RuntimeContext<*, F>, parentContext: ParentContext<F>) =
    asSequence()
        .map { it.toDynamicNode(parentContext) }
        .asStream()
        .onClose { parent.close() }

private fun <PF, F> RuntimeNode<PF, F>.toDynamicNode(parentContext: ParentContext<PF>): DynamicNode = when (this) {
    is RuntimeTest<*> -> dynamicTest(name) {
        parentContext.newRunTest(this as Test<PF>, parentContext.andThenJust(this.name))
    }
    is RuntimeContext -> dynamicContainer(name, this.toStreamOfDynamicNodes(parentContext))
}



