package dev.minutest.junit

import dev.minutest.Node
import dev.minutest.RootContextBuilder
import kotlin.reflect.full.memberFunctions


@Suppress("UNCHECKED_CAST")
internal fun Any.testMethods(): List<RootContextBuilder<*>> = this::class.memberFunctions
    .filter { it.returnType.classifier == RootContextBuilder::class }
    .map { it.call(this) as RootContextBuilder<*> }

internal fun Any.rootContextFromMethods(): Node<Unit> {
    val testMethodsAsNodes: List<RootContextBuilder<*>> = testMethods()
    val singleNode = when {
        testMethodsAsNodes.isEmpty() -> error("No test methods found")
        testMethodsAsNodes.size > 1 -> error("More that one test method is not yet supported")
        else -> testMethodsAsNodes.first()
    }
    return singleNode.buildNode()
}
