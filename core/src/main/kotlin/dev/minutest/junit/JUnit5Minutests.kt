package dev.minutest.junit

import dev.minutest.Context
import dev.minutest.Node
import dev.minutest.RootContextBuilder
import dev.minutest.Test
import dev.minutest.internal.RootExecutor
import dev.minutest.internal.SourceReference
import dev.minutest.internal.TestExecutor
import dev.minutest.internal.rootContextFromMethods
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.File
import java.net.URI
import java.util.stream.Stream
import kotlin.streams.asStream

/**
 * Mix-in this interface to run your tests with JUnit 5
 */
interface JUnit5Minutests {

    /**
     * Provided so that JUnit will run the tests
     */
    @TestFactory
    fun minutests(): Stream<out DynamicNode> =
        this.rootContextFromMethods().toStreamOfDynamicNodes(RootExecutor)
}

/**
 * Convert a root context into a JUnit 5 [@org.junit.jupiter.api.TestFactory].
 *
 * @see [RootContextBuilder#testFactory()]
 */
fun testFactoryFor(root: RootContextBuilder): Stream<out DynamicNode> =
    root.buildNode().toStreamOfDynamicNodes(RootExecutor)

/**
 * Convert a root context into a JUnit 5 [@org.junit.jupiter.api.TestFactory]
 *
 * @see [testFactoryFor(RootContextBuilder)]
 */
fun RootContextBuilder.toTestFactory(): Stream<out DynamicNode> =
    testFactoryFor(this)

// These are defined as extensions to avoid taking a dependency on JUnit in the main package

private fun <F> Node<F>.toStreamOfDynamicNodes(executor: TestExecutor<F>) =
    when (this) {
        is Context<F, *> -> this.toStreamOfDynamicNodes(executor)
        is Test<F> -> Stream.of(this.toDynamicNode(executor))
    }

private fun <PF, F> Context<PF, F>.toStreamOfDynamicNodes(executor: TestExecutor<PF>) =
    children.toStreamOfDynamicNodes(this, executor.andThen(this))

private fun <F> Iterable<Node<F>>.toStreamOfDynamicNodes(
    parent: Context<*, F>,
    executor: TestExecutor<F>
) =
    asSequence()
        .map { it.toDynamicNode(executor) }
        .asStream()
        .onClose { parent.close() }

private fun <F> Node<F>.toDynamicNode(executor: TestExecutor<F>): DynamicNode =
    when (this) {
        is Test<F> -> dynamicTest(name, this.testUri()) {
            executor.runTest(this)
        }
        is Context<F, *> -> dynamicContainer(
            name,
            this.testUri(),
            this.toStreamOfDynamicNodes(executor)
        )
    }

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