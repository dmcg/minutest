package uk.org.minutest.junit

import uk.org.minutest.internal.TopLevelContextBuilder
import kotlin.reflect.full.memberFunctions


@Suppress("UNCHECKED_CAST")
internal fun Any.testMethods(): List<TopLevelContextBuilder<*>> = this::class.memberFunctions
    .filter { it.returnType.classifier == TopLevelContextBuilder::class }
    .map { it.call(this) as TopLevelContextBuilder<*> }

internal fun Any.rootContextFromMethods(): uk.org.minutest.Node<Unit> {
    val testMethodsAsNodes: List<TopLevelContextBuilder<*>> = testMethods()
    val singleNode = when {
        testMethodsAsNodes.isEmpty() -> error("No test methods found")
        testMethodsAsNodes.size > 1 -> error("More than one test method found")
        else -> testMethodsAsNodes.first()
    }
    return singleNode.buildNode()
}
