package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.Named
import com.oneeyedmen.minutest.NodeBuilder
import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest
import org.opentest4j.TestAbortedException
import kotlin.reflect.KClass
import kotlin.reflect.full.safeCast

abstract class AnnotationInterpreter<A: Any>(
    private val annotationClass: KClass<A>
) : (RuntimeNode) -> RuntimeNode {
    
    protected fun appliesTo(node: RuntimeNode) =
        this in node.properties
    
    protected val RuntimeNode.annotation: A get() =
        annotationClass.safeCast(properties[this]) ?: defaultAnnotationValue()
    
    abstract fun defaultAnnotationValue(): A
}

abstract class Annotation<A: Annotation<A>> {
    abstract val transform: AnnotationInterpreter<A>
    
    fun applyTo(properties: MutableMap<Any, Any>) {
        properties[transform] = this
    }
}

fun Context<*,*>.annotateWith(annotation: Annotation<*>) {
    annotation.applyTo(properties)
}

operator fun <F> Annotation<*>.minus(nodeBuilder: NodeBuilder<F>): NodeBuilder<F> {
    this.applyTo(nodeBuilder.properties)
    return nodeBuilder
}

internal fun RuntimeContext.mapChildren(f: (RuntimeNode) -> RuntimeNode) = this.withChildren(this.children.map(f))

internal fun skippingContext(properties: Map<Any, Any>, name: String, parent: Named?) =
    PlainContext(properties, "Skipped $name", parent).apply {
        withChildren(listOf(SkippingTest(this, properties)))
    }

internal data class PlainContext(
    override val properties: Map<Any, Any>,
    override val name: String,
    override val parent: Named?,
    override val children: List<RuntimeNode> = emptyList()
) : RuntimeContext() {
    override fun withChildren(children: List<RuntimeNode>) = copy(children = children)
}

internal class SkippingTest(override val parent: Named, override val properties: Map<Any, Any>) : RuntimeTest() {
    override val name = "skipped"
    override fun run() {
        throw TestAbortedException("skipped")
    }
}

internal class SkippedTest(
    override val name: String,
    override val parent: Named?, override val properties: Map<Any, Any>
) : RuntimeTest() {
    override fun run() {
        throw TestAbortedException("skipped")
    }
}

