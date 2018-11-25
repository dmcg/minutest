package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.*
import com.oneeyedmen.minutest.internal.asKType
import com.oneeyedmen.minutest.internal.topLevelContext
import com.oneeyedmen.minutest.junit.toStreamOfDynamicNodes
import org.junit.jupiter.api.DynamicNode
import org.opentest4j.TestAbortedException
import java.util.stream.Stream

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

operator fun Annotation.minus(nodeBuilder: NodeBuilder<*>): NodeBuilder<*> {
    this.applyTo(nodeBuilder.properties)
    return nodeBuilder
}


inline fun <reified F> Any.transformedJunitTests(transform: (RuntimeNode) -> RuntimeNode, noinline builder: Context<Unit, F>.() -> Unit): Stream<out DynamicNode> =
    topLevelContext(javaClass.canonicalName, asKType<F>(), builder).run(transform).toStreamOfDynamicNodes()


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

