package dev.minutest.junit

import dev.minutest.Node
import dev.minutest.internal.*
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import java.io.File
import java.net.URI

// These are defined as extensions to avoid taking a dependency on JUnit in the main package

internal fun Node<Unit>.toRootListOfDynamicNodes(): List<DynamicNode> =
    when (val runnableNode = this.toRootRunnableNode()) {
        is RunnableTest -> listOf(runnableNode.toDynamicTest())
        is RunnableContext -> runnableNode.toListOfDynamicNodes()
    }

private fun RunnableNode.toDynamicNode() =
    when (this) {
        is RunnableTest -> this.toDynamicTest()
        is RunnableContext -> this.toDynamicContainer()
    }

private fun RunnableTest.toDynamicTest() =
    DynamicTest.dynamicTest(name, testUri) {
        this.invoke()
    }

private fun RunnableContext.toDynamicContainer() =
    DynamicContainer.dynamicContainer(
        name,
        testUri,
        toListOfDynamicNodes().stream()
    )

private fun RunnableContext.toListOfDynamicNodes(): List<DynamicNode> =
    children.map { it.toDynamicNode() }

private val RunnableNode.testUri: URI? get() = sourceReference?.toURI()

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