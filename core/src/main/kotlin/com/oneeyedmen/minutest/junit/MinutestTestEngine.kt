package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.experimental.TopLevelContextBuilder
import com.oneeyedmen.minutest.internal.RuntimeContextWithFixture
import com.oneeyedmen.minutest.internal.RuntimeTestWithFixture
import org.junit.platform.engine.*
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
import kotlin.collections.LinkedHashSet
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmName


class MinutestTestEngine : TestEngine {
    override fun getId() = Companion.id
    
    override fun discover(discoveryRequest: EngineDiscoveryRequest, uniqueId: UniqueId): TestDescriptor {
        val root = MinutestEngineDescriptor(uniqueId, discoveryRequest)
        scan(discoveryRequest).forEach(root::addChild)
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
    
    private fun executeMinutestNode(descriptor: TestDescriptor, node: RuntimeNode, request: EngineDiscoveryRequest, listener: EngineExecutionListener) {
        when (node) {
            is RuntimeContextWithFixture<*, *> -> {
                node.childrenAsDescriptors().forEach { child ->
                    descriptor.addChild(child)
                    if (request.selectsByUniqueId(child)) {
                        listener.dynamicTestRegistered(child)
                        execute(child, request, listener)
                    }
                    else {
                        descriptor.removeChild(child)
                    }
                }
            }
            is RuntimeTestWithFixture<*> -> {
                node.run()
            }
        }
    }
    
    private fun RuntimeContextWithFixture<*, *>.childrenAsDescriptors() =
        children.map { MinutestNodeDescriptor(it) }
    
    private fun executeDiscoveredChildTests(test: TestDescriptor, request: EngineDiscoveryRequest, listener: EngineExecutionListener) {
        test.children.forEach { child ->
            if (request.selectsByUniqueId(child)) {
                execute(child, request, listener)
            }
        }
    }
    
    companion object {
        val id = "minutest"
    }
}

class MinutestEngineDescriptor(uniqueId: UniqueId, val discoveryRequest: EngineDiscoveryRequest) :
    EngineDescriptor(uniqueId, "Minutest")


private val packageType = "minutest-package"
private val contextType = "minutest-context"
private val testType = "minutest-test"


internal sealed class MinutestDescriptor : TestDescriptor {
    private var _parent: TestDescriptor? = null
    private val _children = LinkedHashSet<TestDescriptor>()
    
    final override fun getParent() =
        Optional.ofNullable(_parent)
    
    final override fun setParent(parent: TestDescriptor?) {
        _parent = parent
    }
    
    final override fun getUniqueId() =
        _parent?.uniqueId?.append(idType, id) ?: UniqueId.root(idType, id)
    
    abstract val idType: String
    abstract val id: String
    
    override fun mayRegisterTests(): Boolean = true
    
    final override fun getChildren(): Set<TestDescriptor> {
        return _children.toSet()
    }
    
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
    
    override fun findByUniqueId(uniqueId: UniqueId?) =
        Optional.empty<TestDescriptor>()
    
    override fun getTags() =
        emptySet<TestTag>()
    
}

internal class TestPackageDescriptor(
    private val packageName: String,
    children: List<TopLevelContextDescriptor>
) : MinutestDescriptor() {
    init {
        children.forEach { addChild(it) }
    }
    
    override fun getType(): Type = CONTAINER
    override fun getDisplayName() = packageName
    override val idType: String = packageType
    override val id: String = packageName
    override fun getSource(): Optional<TestSource> =
//        Optional.of(PackageSource.from(packageName))
        Optional.empty() // True implementation commented out because Gradle doesn't handle it correctly
}


internal class TopLevelContextDescriptor(
    val property: KProperty0<TopLevelContextBuilder>
) : MinutestDescriptor() {
    
    override fun getType(): Type = CONTAINER
    override fun getDisplayName() = property.name
    override val idType: String = contextType
    override val id: String = property.name
    override fun getSource(): Optional<TestSource> =
//        Optional.ofNullable(MethodSource.from(property.getter.javaMethod))
        Optional.ofNullable(ClassSource.from(property.getter.javaMethod?.declaringClass))  // True implementation commented out because Gradle doesn't handle it correctly
    
    fun instantiate() = property.get().build(property.name)
}


internal class MinutestNodeDescriptor(
    val node: RuntimeNode
) : MinutestDescriptor() {
    
    override fun getType() = when (node) {
        is RuntimeContext -> CONTAINER
        is RuntimeTest -> TEST
    }
    
    override val idType: String = when (node) {
        is RuntimeContext -> contextType
        is RuntimeTest -> testType
    }
    
    override val id: String = node.name
    override fun getDisplayName() = node.name
    override fun getSource() = Optional.empty<TestSource>()
}


private fun EngineDiscoveryRequest.selectsByUniqueId(descriptor: TestDescriptor) =
    getSelectorsByType<UniqueIdSelector>()
        .run { isEmpty() || any { selector -> descriptor.uniqueId.overlaps(selector.uniqueId) } }

private fun UniqueId.overlaps(that: UniqueId) =
    this.hasPrefix(that) || that.hasPrefix(this)
