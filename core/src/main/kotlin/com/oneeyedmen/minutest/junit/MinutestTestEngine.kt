package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.experimental.TopLevelContextBuilder
import com.oneeyedmen.minutest.internal.PreparedRuntimeContext
import com.oneeyedmen.minutest.internal.PreparedRuntimeTest
import org.junit.platform.engine.*
import org.junit.platform.engine.TestDescriptor.Type
import org.junit.platform.engine.TestDescriptor.Type.CONTAINER
import org.junit.platform.engine.TestDescriptor.Type.TEST
import org.junit.platform.engine.support.descriptor.ClassSource
import org.junit.platform.engine.support.descriptor.EngineDescriptor
import org.opentest4j.AssertionFailedError
import java.util.Optional
import kotlin.collections.LinkedHashSet
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.javaMethod


class MinutestTestEngine : TestEngine {
    override fun getId() = Companion.id
    
    override fun discover(discoveryRequest: EngineDiscoveryRequest, uniqueId: UniqueId): TestDescriptor {
        val root = MinutestEngineDescriptor(uniqueId)
        scan(discoveryRequest).forEach(root::addChild)
        return root
    }
    
    override fun execute(request: ExecutionRequest) {
        execute(request.rootTestDescriptor, request.engineExecutionListener)
    }
    
    private fun execute(test: TestDescriptor, listener: EngineExecutionListener) {
        listener.executionStarted(test)
        val result = try {
            when (test) {
                is TopLevelContextDescriptor ->
                    executeTopLevelContextDescriptor(test, listener)
                is MinutestNodeDescriptor ->
                    executeNodeDescriptor(test, listener)
                else ->
                    executeChildren(test, listener)
            }
            TestExecutionResult.successful()
        }
        catch (e: AssertionFailedError) {
            TestExecutionResult.failed(e)
        }
        catch (t: Throwable) {
            TestExecutionResult.aborted(t)
        }
        
        listener.executionFinished(test, result)
    }
    
    private fun executeTopLevelContextDescriptor(descriptor: TopLevelContextDescriptor, listener: EngineExecutionListener) {
        executeMinutestNode(descriptor, descriptor.instantiate(), listener)
    }
    
    private fun executeNodeDescriptor(descriptor: MinutestNodeDescriptor, listener: EngineExecutionListener) {
        executeMinutestNode(descriptor, descriptor.node, listener)
    }
    
    private fun executeMinutestNode(descriptor: TestDescriptor, node: RuntimeNode, listener: EngineExecutionListener) {
        when (node) {
            is PreparedRuntimeContext<*, *> -> {
                childDescriptors(node).forEach { child ->
                    descriptor.addChild(child)
                    listener.dynamicTestRegistered(child)
                    
                    execute(child, listener)
                }
            }
            is PreparedRuntimeTest<*> -> {
                node.run()
            }
        }
    }
    
    private fun childDescriptors(context: PreparedRuntimeContext<*, *>) =
        context.children.map { MinutestNodeDescriptor(it) }
    
    private fun executeChildren(test: TestDescriptor, listener: EngineExecutionListener) {
        test.children.forEach {
            execute(it, listener)
        }
    }
    
    companion object {
        val id = "minutest"
    }
}

class MinutestEngineDescriptor(uniqueId: UniqueId) :
    EngineDescriptor(uniqueId, "Minutest")


sealed class MinutestDescriptor(
    protected var _parent: TestDescriptor? = null
) : TestDescriptor {
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

class TestPackageDescriptor(
    private val packageName: String,
    children: List<TopLevelContextDescriptor>
): MinutestDescriptor() {
    init {
        children.forEach { addChild(it) }
    }
    
    override fun getType(): Type = CONTAINER
    override fun getDisplayName() = packageName
    override val idType: String = "minutest-package"
    override val id: String = packageName
    override fun getSource(): Optional<TestSource> =
//        Optional.of(PackageSource.from(packageName))
        Optional.empty() // True implementation commented out because Gradle doesn't handle it correctly
    
}

class TopLevelContextDescriptor(
    val property: KProperty0<TopLevelContextBuilder>
) : MinutestDescriptor() {
    
    override fun getType(): Type = CONTAINER
    override fun getDisplayName() = property.name
    override val idType: String = "minutest-property"
    override val id: String = property.name
    override fun getSource(): Optional<TestSource> =
//        Optional.ofNullable(MethodSource.from(property.getter.javaMethod))
        Optional.ofNullable(ClassSource.from(property.getter.javaMethod?.declaringClass))  // True implementation commented out because Gradle doesn't handle it correctly
    
    fun instantiate() = property.get().build(property.name)
}

class MinutestNodeDescriptor(
    val node: RuntimeNode
) : MinutestDescriptor() {
    
    override fun getType() = when (node) {
        is RuntimeContext -> CONTAINER
        is RuntimeTest -> TEST
    }
    
    override val idType: String = when (node) {
        is RuntimeContext -> "minutest-context"
        is RuntimeTest -> "minutest-test"
    }
    
    override val id: String = node.name
    override fun getDisplayName() = node.name
    override fun getSource() = Optional.empty<TestSource>()
}

