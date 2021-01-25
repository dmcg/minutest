package dev.minutest.junit

import dev.minutest.Context
import dev.minutest.internal.*
import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import java.io.File
import java.net.URI
import java.util.stream.Stream
import kotlin.streams.asStream

// These are defined as extensions to avoid taking a dependency on JUnit in the main package

internal fun Context<Unit, *>.toRootStreamOfDynamicNodes(): Stream<DynamicNode> =
    toRootContext().toStreamOfDynamicNodes()

private fun RunnableContext.toStreamOfDynamicNodes(): Stream<DynamicNode> =
    children
        .map { it.toDynamicNode() }
        .asStream()
        .onClose { close() }

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
        toStreamOfDynamicNodes()
    )

internal val RunnableNode.testUri: URI? get() = sourceReference?.toUri()

private fun SourceReference.toUri(): URI = File(path).toURI().let { fileUri ->
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