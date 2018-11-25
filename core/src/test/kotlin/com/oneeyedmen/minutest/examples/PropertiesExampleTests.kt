package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.*
import com.oneeyedmen.minutest.internal.NodeBuilder
import com.oneeyedmen.minutest.internal.PreparedRuntimeContext
import com.oneeyedmen.minutest.internal.asKType
import com.oneeyedmen.minutest.internal.topLevelContext
import com.oneeyedmen.minutest.junit.toStreamOfDynamicNodes
import org.junit.Assert.fail
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.TestFactory
import org.opentest4j.TestAbortedException
import java.util.stream.Stream


class PropertiesExampleTests {

    @TestFactory fun skipRoot() = transformedJunitTests<Unit>(SKIP) {
        annotateWith(SKIP)

        test("won't run") {
            fail()
        }
    }

    @TestFactory fun skipContext() = transformedJunitTests<Unit>(SKIP) {

        test("will run") {}

        SKIP - context("skipped") {
            test("won't run") {
                fail()
            }
        }

    }

    @TestFactory fun focusContext() = transformedJunitTests<Unit>(FOCUS) {

        FOCUS - context("focused") {
            test("will run") {}
        }

        context("not focused") {
            test("won't run") {
                fail()
            }

            FOCUS - test("focused test inside not focused will run") {}

            FOCUS - context("focused inside not focused") {
                test("will run") {}
            }
        }

        context("another way of specifying focused") {
            annotateWith(FOCUS)
            test("will run") {}
        }
    }
}

fun Context<*, *>.annotateWith(annotation: Annotation) {
    annotation.applyTo(properties)
}

data class Annotation(
    private val transform: (RuntimeNode) -> RuntimeNode
) : (RuntimeNode) -> RuntimeNode by transform {
    fun applyTo(properties: MutableMap<Any, Any>) {
        properties[this] = true
    }
    fun appliesTo(properties: Map<Any, Any>) = properties[this] == true
}

val SKIP = Annotation(::skipFilter)
val FOCUS = Annotation(::focusFilter)

operator fun Annotation.minus(nodeBuilder: NodeBuilder<*>): NodeBuilder<*> {
    this.applyTo(nodeBuilder.properties)
    return nodeBuilder
}

fun skipFilter(node: RuntimeNode): RuntimeNode = when (node) {
    is RuntimeContext -> skipFilter(node)
    is RuntimeTest -> node
}

fun skipFilter(context: RuntimeContext): RuntimeNode =
    if (SKIP.appliesTo(context.properties))
        SkippedContext(context.properties, "Skipped ${context.name}", context.parent)
    else
        context.mapChildren(::skipFilter)

fun focusFilter(node: RuntimeNode): RuntimeNode = when (node) {
    is RuntimeContext -> focusFilter(node)
    is RuntimeTest -> node
}

fun focusFilter(context: RuntimeContext): RuntimeNode =
    if (context.children.hasFocus())
        context.mapChildren(RuntimeNode::skipUnlessFocused)
    else context

private fun Iterable<RuntimeNode>.hasFocus(): Boolean = this.find { it.hasFocus() } != null

private fun RuntimeNode.hasFocus() =
    when (this) {
        is RuntimeTest -> FOCUS.appliesTo(properties)
        is RuntimeContext -> FOCUS.appliesTo(properties) || this.children.hasFocus()
    }

private fun RuntimeNode.skipUnlessFocused(): RuntimeNode =
    when (this) {
        is RuntimeTest -> when {
            FOCUS.appliesTo(properties)-> this
            else -> SkippedTest(this.name, this.parent, properties)
        }
        is RuntimeContext -> when {
            FOCUS.appliesTo(properties) -> this
            this.children.hasFocus() -> this.mapChildren(RuntimeNode::skipUnlessFocused)
            else -> SkippedContext(properties, "Skipped $name", parent)
        }
    }


class SkippedContext(
    override val properties: Map<Any, Any>,
    override val name: String,
    override val parent: Named?
) : RuntimeContext() {
    override val children = listOf(SkippingTest(this, properties))
}

class SkippingTest(override val parent: Named, override val properties: Map<Any, Any>) : RuntimeTest() {
    override val name = "skipped"
    override fun run() {
        throw TestAbortedException("skipped")
    }
}

class SkippedTest(
    override val name: String,
    override val parent: Named?, override val properties: Map<Any, Any>
) : RuntimeTest() {
    override fun run() {
        throw TestAbortedException("skipped")
    }
}

private fun RuntimeContext.mapChildren(f: (RuntimeNode) -> RuntimeNode) =
    (this as PreparedRuntimeContext<*, *>).copy(children = children.map(f))

inline fun <reified F> Any.transformedJunitTests(transform: (RuntimeNode) -> RuntimeNode, noinline builder: Context<Unit, F>.() -> Unit): Stream<out DynamicNode> =
    topLevelContext(javaClass.canonicalName, asKType<F>(), builder).run(transform).toStreamOfDynamicNodes()