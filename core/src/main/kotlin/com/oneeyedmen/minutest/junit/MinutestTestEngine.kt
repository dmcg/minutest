package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.experimental.TopLevelContextBuilder
import org.junit.platform.engine.EngineDiscoveryRequest
import org.junit.platform.engine.EngineExecutionListener
import org.junit.platform.engine.ExecutionRequest
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.TestDescriptor.Type
import org.junit.platform.engine.TestDescriptor.Type.CONTAINER
import org.junit.platform.engine.TestDescriptor.Type.TEST
import org.junit.platform.engine.TestEngine
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.TestSource
import org.junit.platform.engine.TestTag
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.discovery.UniqueIdSelector
import org.junit.platform.engine.support.descriptor.ClassSource
import org.junit.platform.engine.support.descriptor.EngineDescriptor
import org.opentest4j.AssertionFailedError
import java.util.Optional
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmName


class MinutestTestEngine : TestEngine {
    override fun getId() = engineId
    
    override fun discover(discoveryRequest: EngineDiscoveryRequest, uniqueId: UniqueId): TestDescriptor {
        val root = MinutestEngineDescriptor(uniqueId, discoveryRequest)
        scan(root, discoveryRequest).forEach(root::addChild)
        return root
    }
    
    override fun execute(request: ExecutionRequest) {
        val root = request.rootTestDescriptor
        if (root is MinutestEngineDescriptor) {
            execute(root, root.discoveryRequest, request.engineExecutionListener)
        }
        else {
            throw IllegalArgumentException("root descriptor is not a ${MinutestEngineDescriptor::class.jvmName}")
        }
    }
    
    private fun execute(descriptor: TestDescriptor, request: EngineDiscoveryRequest, listener: EngineExecutionListener) {
        listener.executionStarted(descriptor)
        val result = try {
            when (descriptor) {
                is TopLevelContextDescriptor ->
                    executeTopLevelContextDescriptor(descriptor, request, listener)
                is MinutestNodeDescriptor ->
                    executeNodeDescriptor(descriptor, request, listener)
                else ->
                    executeDiscoveredChildTests(descriptor, request, listener)
            }
            TestExecutionResult.successful()
        }
        catch (e: AssertionFailedError) {
            TestExecutionResult.failed(e)
        }
        catch (t: Throwable) {
            TestExecutionResult.aborted(t)
        }
        
        listener.executionFinished(descriptor, result)
    }
    
    private fun executeTopLevelContextDescriptor(descriptor: TopLevelContextDescriptor, request: EngineDiscoveryRequest, listener: EngineExecutionListener) {
        executeMinutestNode(descriptor, descriptor.instantiate(), request, listener)
    }
    
    private fun executeNodeDescriptor(descriptor: MinutestNodeDescriptor, request: EngineDiscoveryRequest, listener: EngineExecutionListener) {
        executeMinutestNode(descriptor, descriptor.node, request, listener)
    }
    
    private fun executeMinutestNode(descriptor: TestDescriptor, node: RuntimeNode, request: EngineDiscoveryRequest, listener: EngineExecutionListener) =
        when (node) {
            is RuntimeContext -> {
                childrenAsDescriptors(descriptor, node).forEach { child ->
                    if (request.selectsByUniqueId(child)) {
                        descriptor.addChild(child)
                        listener.dynamicTestRegistered(child)
                        execute(child, request, listener)
                    }
                }
            }
            is RuntimeTest ->
                node.run()
        }
    
    private fun childrenAsDescriptors(parentDescriptor: TestDescriptor, parentNode: RuntimeContext) =
        parentNode.children.map { node -> MinutestNodeDescriptor(parentDescriptor, node) }
    
    private fun executeDiscoveredChildTests(test: TestDescriptor, request: EngineDiscoveryRequest, listener: EngineExecutionListener) {
        test.children.forEach { child ->
            if (request.selectsByUniqueId(child)) {
                execute(child, request, listener)
            }
        }
    }
    
    companion object {
        val engineId = "minutest"
    }
}

class MinutestEngineDescriptor(uniqueId: UniqueId, val discoveryRequest: EngineDiscoveryRequest) :
    EngineDescriptor(uniqueId, "Minutest")


private val packageType = "minutest-package"
private val contextType = "minutest-context"
private val testType = "minutest-test"


internal sealed class MinutestDescriptor(parent: TestDescriptor, idType: String, id: String) : TestDescriptor {
    private var _parent: TestDescriptor? = parent
    private val _children = LinkedHashSet<TestDescriptor>()
    private val _uniqueId = parent.uniqueId.append(idType, id)
    
    final override fun getUniqueId() = _uniqueId
    final override fun getParent() = Optional.ofNullable(_parent)
    final override fun setParent(parent: TestDescriptor?) {
        _parent = parent
    }
    
    final override fun mayRegisterTests(): Boolean = true
    final override fun getChildren() = _children.toMutableSet()
    
    override fun addChild(descriptor: TestDescriptor) {
        _children.add(descriptor)
        descriptor.setParent(this)
    }
    
    override fun removeChild(descriptor: TestDescriptor) {
        _children.remove(descriptor)
        descriptor.setParent(null)
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

internal class TestPackageDescriptor(
    parent: TestDescriptor,
    private val packageName: String,
    children: List<TopLevelContextDescriptor> = emptyList()
) : MinutestDescriptor(parent, packageType, packageName) {
    init {
        children.forEach { addChild(it) }
    }
    
    override fun getType(): Type = CONTAINER
    override fun getDisplayName() = packageName
    override fun getSource(): Optional<TestSource> =
//        Optional.of(PackageSource.from(packageName))
        Optional.empty() // True implementation commented out because Gradle doesn't handle it correctly
}


internal class TopLevelContextDescriptor(
    parent: TestDescriptor,
    val property: KProperty0<TopLevelContextBuilder>
) : MinutestDescriptor(parent, contextType, property.name) {
    
    override fun getType(): Type = CONTAINER
    override fun getDisplayName() = property.name
    override fun getSource(): Optional<TestSource> =
//        Optional.ofNullable(MethodSource.from(property.getter.javaMethod))
        Optional.ofNullable(ClassSource.from(property.getter.javaMethod?.declaringClass))  // True implementation commented out because Gradle doesn't handle it correctly
    
    fun instantiate() = property.get().build(property.name)
}


internal class MinutestNodeDescriptor(
    parent: TestDescriptor,
    val node: RuntimeNode
) : MinutestDescriptor(parent, node.descriptorIdType(), node.name) {
    override fun getType() = when (node) {
        is RuntimeContext -> CONTAINER
        is RuntimeTest -> TEST
    }
    
    override fun getDisplayName() = node.name
    override fun getSource() = Optional.empty<TestSource>()
}

private fun RuntimeNode.descriptorIdType(): String {
    return when (this) {
        is RuntimeContext -> contextType
        is RuntimeTest -> testType
    }
}

private fun EngineDiscoveryRequest.selectsByUniqueId(descriptor: TestDescriptor) =
    getSelectorsByType<UniqueIdSelector>()
        .run { isEmpty() || any { selector -> descriptor.uniqueId.overlaps(selector.uniqueId) } }

private fun UniqueId.overlaps(that: UniqueId) =
    this.hasPrefix(that) || that.hasPrefix(this)
