package dev.minutest.junit

import dev.minutest.Node
import dev.minutest.RootContextBuilder
import dev.minutest.internal.*
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.File
import java.net.URI

/**
 * Mix-in this interface to run your tests with JUnit 5
 */
interface JUnit5Minutests {

    /**
     * Provided so that JUnit will run the tests
     */
    @TestFactory
    fun minutests(): Iterable<DynamicNode> =
        rootContextFromMethods()
            .toRootListOfDynamicNodes()
}

/**
 * Convert a root context into a JUnit 5 [@org.junit.jupiter.api.TestFactory].
 *
 * @see [RootContextBuilder#testFactory()]
 */
fun testFactoryFor(root: RootContextBuilder): Iterable<DynamicNode> =
    root.buildNode().toRootListOfDynamicNodes()

/**
 * Convert a root context into a JUnit 5 [@org.junit.jupiter.api.TestFactory]
 *
 * @see [testFactoryFor(RootContextBuilder)]
 */
fun RootContextBuilder.toTestFactory(): Iterable<DynamicNode> =
    testFactoryFor(this)

// These are defined as extensions to avoid taking a dependency on JUnit in the main package

private fun RunnableNode.toDynamicNode() =
    when (this) {
        is RunnableTest -> this.toDynamicTest()
        is RunnableContext -> this.toDynamicContainer()
    }

private fun RunnableTest.toDynamicTest() =
    dynamicTest(name, test.testUri()) {
        this.invoke()
    }

private fun RunnableContext.toDynamicContainer() =
    dynamicContainer(
        name,
        context.testUri(),
        toListOfDynamicNodes().stream()
    )

private fun Node<Unit>.toRootListOfDynamicNodes(): List<DynamicNode> =
    when (val runnableNode = this.toRootRunnableNode()) {
        is RunnableTest -> listOf(runnableNode.toDynamicTest())
        is RunnableContext -> runnableNode.toListOfDynamicNodes()
    }

private fun RunnableContext.toListOfDynamicNodes(): List<DynamicNode> =
    children.map { it.toDynamicNode() }

private fun <F> Node<F>.testUri(): URI? =
    (this.markers.find { it is SourceReference } as? SourceReference)?.toURI()

private fun SourceReference.toURI(): URI = File(path).toURI().let { fileUri ->
    URI(
        fileUri.scheme,
        fileUri.userInfo,
        fileUri.host,
        fileUri.port,
        "//" + fileUri.path,
        "line=$lineNumber",
        fileUri.fragment
    )
}