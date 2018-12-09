package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest
import org.junit.platform.engine.EngineDiscoveryRequest
import org.junit.platform.engine.EngineExecutionListener
import org.junit.platform.engine.ExecutionRequest
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.TestDescriptor.Type.CONTAINER
import org.junit.platform.engine.TestDescriptor.Type.TEST
import org.junit.platform.engine.TestEngine
import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.engine.TestSource
import org.junit.platform.engine.TestTag
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.support.descriptor.EngineDescriptor
import org.opentest4j.AssertionFailedError
import org.opentest4j.IncompleteExecutionException
import org.opentest4j.TestAbortedException
import org.opentest4j.TestSkippedException
import java.util.Optional
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
            execute(root, root.discoveryRequest, request.engineExecutionListener)
        }
        else {
            throw IllegalArgumentException("root descriptor is not a ${MinutestEngineDescriptor::class.jvmName}")
        }
    }
    
    private fun execute(descriptor: TestDescriptor, request: EngineDiscoveryRequest, listener: EngineExecutionListener) {
        listener.executionStarted(descriptor)
        val result = try {
            if (descriptor is MinutestNodeDescriptor) {
                when (descriptor.node) {
                    is RuntimeContext -> executeDynamicChildren(descriptor, request, listener)
                    is RuntimeTest -> executeTest(descriptor.node)
                }
            }
            else {
                executeStaticChildren(descriptor, request, listener)
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
    
    private fun executeDynamicChildren(parent: MinutestNodeDescriptor, request: EngineDiscoveryRequest, listener: EngineExecutionListener) {
        parent.childrenAsDescriptors().forEach { child ->
            if (request.selectsByUniqueId(child)) {
                listener.dynamicTestRegistered(child)
                parent.addChild(child)
                execute(child, request, listener)
            }
        }
    }
    
    private fun MinutestNodeDescriptor.childrenAsDescriptors() =
        when (node) {
            is RuntimeContext ->
                node.children.map { child -> MinutestNodeDescriptor(this, child) }
            is RuntimeTest ->
                emptyList()
        }
    
    
    private fun executeStaticChildren(test: TestDescriptor, request: EngineDiscoveryRequest, listener: EngineExecutionListener) {
        test.children.forEach { child ->
            if (request.selectsByUniqueId(child)) {
                execute(child, request, listener)
            }
        }
    }
    
    private fun executeTest(node: RuntimeTest) {
        node.run()
    }
    
    companion object {
        val engineId = "minutest"
    }
}

class MinutestEngineDescriptor(uniqueId: UniqueId, val discoveryRequest: EngineDiscoveryRequest) :
    EngineDescriptor(uniqueId, "Minutest")


private const val contextType = "minutest-context"
private const val testType = "minutest-test"


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


internal class MinutestNodeDescriptor(
    parent: TestDescriptor,
    val node: RuntimeNode,
    private val source: TestSource? = null

) : MinutestDescriptor(parent, node.descriptorIdType(), node.name) {
    
    override fun getType() = when (node) {
        is RuntimeContext -> CONTAINER
        is RuntimeTest -> TEST
    }
    
    override fun getDisplayName() = node.name
    override fun getSource() = Optional.ofNullable(source)
}

private fun RuntimeNode.descriptorIdType(): String {
    return when (this) {
        is RuntimeContext -> contextType
        is RuntimeTest -> testType
    }
}

