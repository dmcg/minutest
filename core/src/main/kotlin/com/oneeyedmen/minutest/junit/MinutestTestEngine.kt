package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.internal.RootContext
import com.oneeyedmen.minutest.internal.TestExecutor
import com.oneeyedmen.minutest.internal.andThenJust
import org.junit.platform.engine.*
import org.junit.platform.engine.TestDescriptor.Type.CONTAINER
import org.junit.platform.engine.TestDescriptor.Type.TEST
import org.junit.platform.engine.discovery.*
import org.junit.platform.engine.support.descriptor.EngineDescriptor
import org.opentest4j.IncompleteExecutionException
import java.util.Optional
import kotlin.collections.LinkedHashSet
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName


class MinutestTestEngine : TestEngine {
    override fun getId() = engineId
    
    override fun discover(discoveryRequest: EngineDiscoveryRequest, uniqueId: UniqueId) =
        MinutestEngineDescriptor(uniqueId, discoveryRequest).apply {
            scan(this, discoveryRequest).forEach { addChild(it) }
        }
    
    override fun execute(request: ExecutionRequest) {
        val root = request.rootTestDescriptor
        if (root is MinutestEngineDescriptor) {
            execute(root, RootContext, root.discoveryRequest, request.engineExecutionListener)
        }
        else {
            throw IllegalArgumentException("root descriptor is not a ${MinutestEngineDescriptor::class.jvmName}")
        }
    }
    
    private fun execute(
        descriptor: TestDescriptor,
        executor: TestExecutor<*>,
        request: EngineDiscoveryRequest,
        listener: EngineExecutionListener
    ) {
        listener.executionStarted(descriptor)
        val result = try {
            if (descriptor is MinutestNodeDescriptor) {
                when (descriptor.node) {
                    is RuntimeContext<*, *> -> executeDynamicChildren(
                        descriptor,
                        TODO(), //parentContext.andThen(descriptor.node),
                        request,
                        listener
                    )
                    is RuntimeTest<*> -> executeTest(descriptor.node, executor)
                }
            }
            else {
                executeStaticChildren(descriptor, executor, request, listener)
            }
            
            TestExecutionResult.successful()
        }
        catch (e: IncompleteExecutionException) {
            TestExecutionResult.aborted(e)
        }
        catch (t: Throwable) {
            TestExecutionResult.failed(t)
        }
        
        listener.executionFinished(descriptor, result)
    }
    
    private fun executeDynamicChildren(
        parent: MinutestNodeDescriptor,
        executor: TestExecutor<*>,
        request: EngineDiscoveryRequest,
        listener: EngineExecutionListener
    ) {
        parent.childrenAsDescriptors().forEach { child ->
            if (request.selectsByUniqueId(child)) {
                listener.dynamicTestRegistered(child)
                parent.addChild(child)
                execute(child, executor, request, listener)
            }
        }
    }
    
    private fun MinutestNodeDescriptor.childrenAsDescriptors() =
        when (node) {
            is RuntimeContext<*, *> ->
                node.children.map { child -> MinutestNodeDescriptor(this, child) }
            is RuntimeTest<*> ->
                emptyList()
        }
    
    
    private fun executeStaticChildren(
        test: TestDescriptor,
        executor: TestExecutor<*>,
        request: EngineDiscoveryRequest,
        listener: EngineExecutionListener
    ) {
        test.children.forEach { child ->
            if (request.selectsByUniqueId(child)) {
                execute(child, executor, request, listener)
            }
        }
    }
    
    private fun executeTest(node: RuntimeTest<*>, executor: TestExecutor<*>) {
        executor.runTest(
            TODO(), //node as Test<Any?>,
            executor.andThenJust(node.name))
    }
    
    companion object {
        const val engineId = "minutest"
    }
}

class MinutestEngineDescriptor(uniqueId: UniqueId, val discoveryRequest: EngineDiscoveryRequest) :
    EngineDescriptor(uniqueId, "Minutest")


private const val contextType = "minutest-context"
private const val testType = "minutest-test"


private class MinutestNodeDescriptor(
    parent: TestDescriptor,
    val node: RuntimeNode<*, *>,
    private val source: TestSource? = null

) : TestDescriptor {
    
    private var _parent: TestDescriptor? = parent
    private val _children = LinkedHashSet<TestDescriptor>()
    private val _uniqueId = parent.uniqueId.append(node.descriptorIdType(), node.name)
    
    override fun getDisplayName() = node.name
    override fun getUniqueId(): UniqueId = _uniqueId
    override fun getSource() = Optional.ofNullable(source)
    override fun getType() = when (node) {
        is RuntimeContext -> CONTAINER
        is RuntimeTest -> TEST
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
        }
        else {
            _children.asSequence().map { findByUniqueId(uniqueId) }.firstOrNull { it.isPresent } ?: Optional.empty()
        }
    
    override fun getTags() = emptySet<TestTag>()
}


private fun RuntimeNode<*, *>.descriptorIdType(): String {
    return when (this) {
        is RuntimeContext -> contextType
        is RuntimeTest -> testType
    }
}

private fun scan(root: MinutestEngineDescriptor, rq: EngineDiscoveryRequest): List<TestDescriptor> {
    // Cannot select by method
    if (rq.getSelectorsByType<MethodSelector>().isNotEmpty()) {
        return emptyList()
    }
    
    return com.oneeyedmen.minutest.internal.scan(
        scannerConfig = {
            rq.forEach<PackageSelector> { whitelistPackages(it.packageName) }
            rq.forEach<ClassSelector> { whitelistClasses(it.className) }
            rq.forEach<DirectorySelector> { whitelistPaths(it.rawPath) }
        },
        classFilter = {
            rq.getFiltersByType<ClassNameFilter>().apply(it.name).included() &&
                rq.getFiltersByType<PackageNameFilter>().apply(it.packageName).included()
        })
        .map { MinutestNodeDescriptor(root, it) }
        .filter { rq.selectsByUniqueId(it) }
}

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

