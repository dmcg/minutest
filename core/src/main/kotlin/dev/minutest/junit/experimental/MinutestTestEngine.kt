package dev.minutest.junit.experimental

import dev.minutest.internal.RunnableContext
import dev.minutest.internal.RunnableTest
import dev.minutest.internal.findRootContextPerPackage
import dev.minutest.internal.toRootRunnableNode
import org.junit.platform.engine.*
import org.junit.platform.engine.discovery.*
import org.junit.platform.engine.support.descriptor.EngineDescriptor
import org.opentest4j.IncompleteExecutionException
import kotlin.reflect.KClass
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
            scan(this, discoveryRequest).forEach {
                addChild(it)
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
    ): TestExecutionResult {
        return try {
            if (descriptor is MinutestNodeDescriptor) {
                when (descriptor.runnableNode) {
                    is RunnableContext ->
                        executeDynamicChildren(
                            descriptor,
                            request,
                            listener
                        )
                    is RunnableTest ->
                        descriptor.runnableNode.invoke()
                }
            } else {
                executeStaticChildren(descriptor, request, listener)
            }
            TestExecutionResult.successful()
        } catch (e: IncompleteExecutionException) {
            TestExecutionResult.aborted(e)
        } catch (t: Throwable) {
            TestExecutionResult.failed(t)
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
            is RunnableTest -> emptyList()
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

private class MinutestEngineDescriptor(
    uniqueId: UniqueId,
    val discoveryRequest: EngineDiscoveryRequest
) : EngineDescriptor(uniqueId, "Minutest")

private fun scan(
    root: MinutestEngineDescriptor,
    discoveryRequest: EngineDiscoveryRequest
): List<TestDescriptor> =
    when {
        discoveryRequest.getSelectorsByType<MethodSelector>().isNotEmpty() ->
            emptyList() // Cannot select by method
        else ->
            findRootContextPerPackage(discoveryRequest)
                .map { rootContext -> MinutestNodeDescriptor(root, rootContext.toRootRunnableNode()) }
                .filter { discoveryRequest.selectsByUniqueId(it) }
    }

private fun findRootContextPerPackage(discoveryRequest: EngineDiscoveryRequest) =
    findRootContextPerPackage(
        scannerConfig = {
            discoveryRequest.forEach<PackageSelector> { whitelistPackages(it.packageName) }
            discoveryRequest.forEach<ClassSelector> { whitelistClasses(it.className) }
            discoveryRequest.forEach<DirectorySelector> { whitelistPaths(it.rawPath) }
        },
        classFilter = {
            discoveryRequest.getFiltersByType<ClassNameFilter>().apply(it.name).included() &&
                discoveryRequest.getFiltersByType<PackageNameFilter>().apply(it.packageName).included()
        }
    )

private inline fun <reified T : DiscoverySelector> EngineDiscoveryRequest.forEach(block: (T) -> Unit) {
    getSelectorsByType<T>().forEach(block)
}

private inline fun <reified T : DiscoverySelector> EngineDiscoveryRequest.getSelectorsByType(): List<T> =
    getSelectorsByType(T::class.java)

private inline fun <reified T : DiscoveryFilter<String>> EngineDiscoveryRequest.getFiltersByType(): Filter<String> =
    combineFiltersByType(T::class)

private fun EngineDiscoveryRequest.combineFiltersByType(filterClass: KClass<out DiscoveryFilter<String>>): Filter<String> =
    Filter.composeFilters(getFiltersByType(filterClass.java))

private fun EngineDiscoveryRequest.selectsByUniqueId(descriptor: TestDescriptor) =
    getSelectorsByType<UniqueIdSelector>()
        .run { isEmpty() || any { selector -> descriptor.uniqueId.overlaps(selector.uniqueId) } }

private fun UniqueId.overlaps(that: UniqueId) =
    this.hasPrefix(that) || that.hasPrefix(this)

