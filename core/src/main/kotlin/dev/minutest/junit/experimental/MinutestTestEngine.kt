package dev.minutest.junit.experimental

import dev.minutest.internal.*
import org.junit.platform.engine.*
import org.junit.platform.engine.TestDescriptor.Type.CONTAINER
import org.junit.platform.engine.TestDescriptor.Type.TEST
import org.junit.platform.engine.discovery.*
import org.junit.platform.engine.support.descriptor.EngineDescriptor
import org.opentest4j.IncompleteExecutionException
import java.util.*
import kotlin.collections.LinkedHashSet
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
    }
}

private class MinutestEngineDescriptor(
    uniqueId: UniqueId,
    val discoveryRequest: EngineDiscoveryRequest
) : EngineDescriptor(uniqueId, "Minutest")

private const val contextType = "minutest-context"
private const val testType = "minutest-test"

private class MinutestNodeDescriptor(
    parent: TestDescriptor,
    val runnableNode: RunnableNode,
    private val source: TestSource? = null
) : TestDescriptor {

    private var _parent: TestDescriptor? = parent
    private val _children = LinkedHashSet<TestDescriptor>()
    private val _uniqueId = parent.uniqueId.append(runnableNode.descriptorIdType(), runnableNode.name)

    override fun getDisplayName() = runnableNode.name
    override fun getUniqueId(): UniqueId = _uniqueId
    override fun getSource() = Optional.ofNullable(source)
    override fun getType() = when (runnableNode) {
        is RunnableContext -> CONTAINER
        is RunnableTest -> TEST
    }

    override fun getParent() = Optional.ofNullable(_parent)
    override fun setParent(parent: TestDescriptor?) {
        _parent = parent
    }

    override fun mayRegisterTests(): Boolean = true
    override fun getChildren() = _children.toMutableSet()

    override fun addChild(descriptor: TestDescriptor) {
        _children.add(descriptor)
        descriptor.setParent(this)
    }

    override fun removeChild(descriptor: TestDescriptor) {
        if (_children.remove(descriptor)) {
            descriptor.setParent(null)
        }
    }

    override fun removeFromHierarchy() {
        _parent?.removeChild(this)
        _parent = null
    }

    override fun findByUniqueId(uniqueId: UniqueId?): Optional<TestDescriptor> =
        if (uniqueId == this._uniqueId) {
            Optional.of(this)
        } else {
            _children.asSequence().map { findByUniqueId(uniqueId) }.firstOrNull { it.isPresent } ?: Optional.empty()
        }

    override fun getTags() = emptySet<TestTag>()
}

private fun RunnableNode.descriptorIdType(): String =
    when (this) {
        is RunnableContext -> contextType
        is RunnableTest -> testType
    }

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

