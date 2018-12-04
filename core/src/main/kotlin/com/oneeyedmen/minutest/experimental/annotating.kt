package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.*
import org.opentest4j.TestAbortedException

data class Annotation(
    private val transform: (RuntimeNode) -> RuntimeNode
) : (RuntimeNode) -> RuntimeNode by transform {
    fun applyTo(properties: MutableMap<Any, Any>) {
        properties[this] = true
    }

    fun appliesTo(properties: Map<Any, Any>) = properties[this] == true
}

fun Context<*, *>.annotateWith(annotation: Annotation) {
    annotation.applyTo(properties)
}

operator fun <F> Annotation.minus(nodeBuilder: NodeBuilder<F>): NodeBuilder<F> {
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
    override fun close() {}

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

