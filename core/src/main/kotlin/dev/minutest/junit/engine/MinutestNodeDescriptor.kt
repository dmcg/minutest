package dev.minutest.junit.engine

import dev.minutest.internal.RunnableContext
import dev.minutest.internal.RunnableNode
import dev.minutest.internal.RunnableTest
import dev.minutest.junit.testUri
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.TestSource
import org.junit.platform.engine.TestTag
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.support.descriptor.UriSource
import java.util.*
import kotlin.collections.LinkedHashSet

/**
 * The way that we describe a context or test to the JUnit 5 platform.
 */
internal class MinutestNodeDescriptor(
    parent: TestDescriptor,
    val runnableNode: RunnableNode,
) : TestDescriptor {

    private var _parent: TestDescriptor? = parent
    private val _children = LinkedHashSet<TestDescriptor>()
    private val _uniqueId = parent.uniqueId.append(runnableNode.descriptorIdType, runnableNode.name)

    override fun getDisplayName() = runnableNode.name
    override fun getUniqueId(): UniqueId = _uniqueId
    override fun getSource() = Optional.ofNullable(runnableNode.testSource)
    override fun getType() = runnableNode.type

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

private val RunnableNode.testSource: TestSource? get() = testUri?.let { UriSource.from(it) }

private val RunnableNode.descriptorIdType: String
    get() = when (this) {
        is RunnableContext -> MinutestTestEngine.contextType
        is RunnableTest -> MinutestTestEngine.testType
    }

private val RunnableNode.type: TestDescriptor.Type
    get() = when (this) {
        is RunnableContext -> TestDescriptor.Type.CONTAINER
        is RunnableTest -> TestDescriptor.Type.TEST
    }

