package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.Test
import com.oneeyedmen.minutest.TestContext
import com.oneeyedmen.minutest.internal.MiContext
import com.oneeyedmen.minutest.internal.MinuTest
import com.oneeyedmen.minutest.internal.Node
import com.oneeyedmen.minutest.internal.Operations
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import java.util.stream.Stream
import kotlin.reflect.KClass
import kotlin.streams.asStream

/**
 * Define a [TestContext] and map it to be used as a JUnit [org.junit.jupiter.api.TestFactory].
 */
inline fun <reified F : Any> junitTests(noinline builder: TestContext<F>.() -> Unit): Stream<out DynamicNode> =
    junitTests(F::class, builder)

fun <F : Any> junitTests(fixtureType: KClass<F>, builder: TestContext<F>.() -> Unit) =
    MiContext("ignored", fixtureType, builder = builder).toDynamicContainer(Operations.empty()).children

// These are defined as extensions to avoid taking a dependency on JUnit in the main package

private fun <F : Any> MiContext<F>.toDynamicContainer(parentOperations: Operations<F>): DynamicContainer =
    dynamicContainer(
        name,
        children.asSequence().map { it.toDynamicNode(this, parentOperations) }.asStream())


private fun <F : Any> Node<F>.toDynamicNode(miContext: MiContext<F>, parentOperations: Operations<F>) = when (this) {
    is MinuTest<F> -> this.toRuntimeTest(miContext, parentOperations).toDynamicTest()
    is MiContext<F> -> this.toDynamicContainer(parentOperations + miContext.operations)
}

private fun <F : Any> Test<F>.toRuntimeTest(miContext: MiContext<F>, parentOperations: Operations<F>) =
    RuntimeTest(this.name) {
        miContext.runTest(this, parentOperations)
    }

private fun <F : Any> MiContext<F>.toRuntimeContext(
    context: MiContext<F>,
    parentOperations: Operations<F>
): RuntimeContext =
    RuntimeContext(this.name, this.children.asSequence().map { it.toRuntimeNode(context, parentOperations) })

private fun <F : Any> Node<F>.toRuntimeNode(context: MiContext<F>, parentOperations: Operations<F>) = when (this) {
    is MinuTest<F> -> this.toRuntimeTest(context, parentOperations)
    is MiContext<F> -> this.toRuntimeContext(context, parentOperations + context.operations)
}


sealed class RuntimeNode
class RuntimeTest(val name: String, val block: () -> Unit) : RuntimeNode()
class RuntimeContext(val name: String, val children: Sequence<RuntimeNode>) : RuntimeNode()

private fun RuntimeTest.toDynamicTest(): DynamicTest = dynamicTest(name) { this.block() }
private fun RuntimeContext.toDynamicContainer(): DynamicContainer = dynamicContainer(
    name,
    children.map { it.toDynamicNode() }.asStream()
)

private fun RuntimeNode.toDynamicNode(): DynamicNode = when (this) {
    is RuntimeTest -> this.toDynamicTest()
    is RuntimeContext -> this.toDynamicContainer()
}
