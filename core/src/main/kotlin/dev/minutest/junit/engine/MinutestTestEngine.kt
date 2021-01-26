package dev.minutest.junit.engine

import dev.minutest.internal.RunnableContext
import dev.minutest.internal.RunnableTest
import dev.minutest.internal.time
import org.junit.platform.engine.*
import org.junit.platform.engine.discovery.UniqueIdSelector
import org.junit.platform.engine.support.descriptor.EngineDescriptor
import org.opentest4j.IncompleteExecutionException
import kotlin.reflect.jvm.jvmName

/**
 * A JUnit 5 platform test engine that runs Minutests separately from JUnit Jupiter.
 */
class MinutestTestEngine : TestEngine {

    override fun getId() = engineId

    override fun discover(
        discoveryRequest: EngineDiscoveryRequest,
        uniqueId: UniqueId
    ): EngineDescriptor =
        MinutestEngineDescriptor(uniqueId, discoveryRequest).apply {
            time("Minutest discovery took ", System.err::println) {
                if (discoveryRequest.selectsByUniqueId(uniqueId))
                    findRootNodes(this, discoveryRequest).forEach {
                        addChild(it)
                    }
            }
        }

    override fun execute(request: ExecutionRequest) {
        val root = request.rootTestDescriptor
        require(root is MinutestEngineDescriptor) {
            "root descriptor is not a ${MinutestEngineDescriptor::class.jvmName}"
        }
        execute(root, root.discoveryRequest, request.engineExecutionListener)
    }

    private fun execute(
        descriptor: TestDescriptor,
        request: EngineDiscoveryRequest,
        listener: EngineExecutionListener
    ) {
        listener.executionStarted(descriptor)
        val result = run(descriptor, request, listener)
        listener.executionFinished(descriptor, result)
    }

    private fun run(
        descriptor: TestDescriptor,
        request: EngineDiscoveryRequest,
        listener: EngineExecutionListener,
    ): TestExecutionResult =
        try {
            if (descriptor is MinutestNodeDescriptor) {
                executeMinutestNode(descriptor, request, listener)
            } else {
                executeStaticChildren(descriptor, request, listener)
            }
            TestExecutionResult.successful()
        } catch (e: IncompleteExecutionException) {
            TestExecutionResult.aborted(e)
        } catch (t: Throwable) {
            TestExecutionResult.failed(t)
        }

    private fun executeMinutestNode(
        descriptor: MinutestNodeDescriptor,
        request: EngineDiscoveryRequest,
        listener: EngineExecutionListener
    ) {
        when (val runnableNode = descriptor.runnableNode) {
            is RunnableContext -> {
                executeDynamicChildren(
                    descriptor,
                    request,
                    listener
                )
                runnableNode.close()
            }
            is RunnableTest ->
                runnableNode.invoke()
        }
    }

    private fun executeDynamicChildren(
        parent: MinutestNodeDescriptor,
        request: EngineDiscoveryRequest,
        listener: EngineExecutionListener
    ) {
        parent.childrenAsDescriptors().forEach { child ->
            if (request.selectsByUniqueId(child)) {
                listener.dynamicTestRegistered(child)
                parent.addChild(child)
                execute(child, request, listener)
            }
        }
    }

    private fun MinutestNodeDescriptor.childrenAsDescriptors() =
        when (runnableNode) {
            is RunnableContext ->
                runnableNode.children.map { child ->
                    MinutestNodeDescriptor(this, child)
                }
            is RunnableTest -> emptySequence()
        }

    private fun executeStaticChildren(
        test: TestDescriptor,
        request: EngineDiscoveryRequest,
        listener: EngineExecutionListener
    ) {
        test.children.forEach { child ->
            if (request.selectsByUniqueId(child)) {
                execute(child, request, listener)
            }
        }
    }

    companion object {
        const val engineId = "minutest"
        internal const val contextType = "minutest-context"
        internal const val testType = "minutest-test"
    }
}

internal class MinutestEngineDescriptor(
    uniqueId: UniqueId,
    val discoveryRequest: EngineDiscoveryRequest
) : EngineDescriptor(uniqueId, "Minutest")

internal fun EngineDiscoveryRequest.selectsByUniqueId(descriptor: TestDescriptor): Boolean =
    selectsByUniqueId(descriptor.uniqueId)

private fun EngineDiscoveryRequest.selectsByUniqueId(uniqueId: UniqueId): Boolean =
    getSelectorsByType<UniqueIdSelector>().run {
        isEmpty() || any { selector -> uniqueId.overlaps(selector.uniqueId) }
    }

private fun UniqueId.overlaps(that: UniqueId) =
    this.hasPrefix(that) || that.hasPrefix(this)

internal inline fun <reified T : DiscoverySelector> EngineDiscoveryRequest.getSelectorsByType()
    : List<T> =
    getSelectorsByType(T::class.java)

