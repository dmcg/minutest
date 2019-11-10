
package dev.minutest.junit

import dev.minutest.Context
import dev.minutest.Node
import dev.minutest.RootContextBuilder
import dev.minutest.Test
import dev.minutest.internal.RootExecutor
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

interface JUnit5Minutests {

    /**
     * Provided so that JUnit will run the tests
     */
    @TestFactory
    fun minutests(): Stream<out DynamicNode> = this.rootContextFromMethods().toStreamOfDynamicNodes(RootExecutor)
}

/**
 * Convert a root context into a JUnit 5 [@org.junit.jupiter.api.TestFactory].
 *
 * @see [RootContextBuilder#testFactory()]
 */
fun testFactoryFor(root: RootContextBuilder): Stream<out DynamicNode> = root.buildNode().toStreamOfDynamicNodes(RootExecutor)

/**
 * Convert a root context into a JUnit 5 [@org.junit.jupiter.api.TestFactory]
 *
 * @see [testFactoryFor(RootContextBuilder)]
 */
fun RootContextBuilder.toTestFactory() = testFactoryFor(this)

// These are defined as extensions to avoid taking a dependency on JUnit in the main package

private fun <F> Node<F>.toStreamOfDynamicNodes(executor: TestExecutor<F>) = when (this) {
    is Context<F, *> -> this.toStreamOfDynamicNodes(executor)
    is Test<F> -> Stream.of(this.toDynamicNode(executor))
}

private fun <PF, F> Context<PF, F>.toStreamOfDynamicNodes(executor: TestExecutor<PF>): Stream<out DynamicNode> =
    children.toStreamOfDynamicNodes(this, executor.andThen(this))

private fun <F> Iterable<Node<F>>.toStreamOfDynamicNodes(parent: Context<*, F>, executor: TestExecutor<F>) =
    asSequence()
        .map { it.toDynamicNode(executor) }
        .asStream()
        .onClose { parent.close() }

private fun <F> Node<F>.toDynamicNode(executor: TestExecutor<F>): DynamicNode = when (this) {
    is Test<F> -> dynamicTest(name, this.testUri()) {
        executor.runTest(this)
    }
    is Context<F, *> -> dynamicContainer(name, this.testUri(), this.toStreamOfDynamicNodes(executor))
}

private fun <F> Node<F>.testUri(): URI? =
    (this.markers.find { it is StackTraceElement } as? StackTraceElement)?.toSourceFileURI(File("src/test/kotlin/"))

// WIP
// If we store the stack trace element of the invocation of ContextBuilder.test in the TestBuilder and then the Test, we can
// populate the DynamicTest testSourceURI and allow navigation on double-click
private fun StackTraceElement.toSourceFileURI(sourceRoot: File): URI? {
    val fileName = fileName ?: return null
    val type = Class.forName(className)
    val fileUri = sourceRoot.toPath().resolve(type.`package`.name.replace(".", "/")).resolve(fileName).toUri()
    return URI(fileUri.scheme, fileUri.userInfo, fileUri.host, fileUri.port, "//" + fileUri.path, "line=$lineNumber", fileUri.fragment)
}



