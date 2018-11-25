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

    @TestFactory fun skipRoot() = filteredJUnitTests<Unit> {
        properties["skip"] = true

        test("won't run") {
            fail()
        }
    }

    @TestFactory fun skipContext() = filteredJUnitTests<Unit> {

        test("will run") {}

        context("skipped") {
            properties["skip"] = true

            test("won't run") {
                fail()
            }
        }

    }

    @TestFactory fun focusContext() = filteredJUnitTests<Unit> {

        context("focused") {

            test("will run") {}
        }

        context("not focused") {
            test("won't run") {
                fail()
            }

            "focus" annotate test("will run") {
                properties["focus"] = true
            }

            FOCUS + context("focused inside not focused") {
                test("will run") {}
            }
        }

        context("another focused") {
            properties["focus"] = true

            test("will run") {}
        }
    }
}

infix fun Pair<String, Any>.annotate(nodeBuilder: NodeBuilder<*>) {
    nodeBuilder.properties.put(this.first, this.second)
}

infix fun String.annotate(nodeBuilder: NodeBuilder<*>) = (this to true).annotate(nodeBuilder)

class Annotation(private val propertyName: String) {
    fun applyTo(nodeBuilder: NodeBuilder<*>): NodeBuilder<*> {
        nodeBuilder.properties[propertyName] = true
        return nodeBuilder
    }
}

val SKIP = Annotation("skip")
val FOCUS = Annotation("focus")

infix fun Annotation.annotate(nodeBuilder: NodeBuilder<*>) = this.applyTo(nodeBuilder)

operator fun Annotation.plus(nodeBuilder: NodeBuilder<*>) = this.applyTo(nodeBuilder)

fun RuntimeNode.filter(): RuntimeNode = when (this) {
    is RuntimeContext -> this.filter()
    is RuntimeTest -> this
}

fun RuntimeContext.filter(): RuntimeNode =
    if (properties["skip"] == true)
        SkippedContext(properties, "Skipped $name", parent)
    else
        this.mapChildren(RuntimeNode::filter)

fun RuntimeNode.focusFilter(): RuntimeNode = when (this) {
    is RuntimeContext -> this.focusFilter()
    is RuntimeTest -> this
}

fun RuntimeContext.focusFilter(): RuntimeNode =
    if (this.children.hasFocus())
        this.mapChildren(RuntimeNode::skipUnlessFocused)
    else this

private fun Iterable<RuntimeNode>.hasFocus(): Boolean = this.find { it.hasFocus() } != null

private fun RuntimeNode.hasFocus() =
    when (this) {
        is RuntimeTest -> this.properties["focus"] == true
        is RuntimeContext -> this.properties["focus"] == true || this.children.hasFocus()
    }

private fun RuntimeNode.skipUnlessFocused(): RuntimeNode =
    when (this) {
        is RuntimeTest -> when {
            this.properties["focus"] == true -> this
            else -> SkippedTest(this.name, this.parent, properties)
        }
        is RuntimeContext -> when {
            this.properties["focus"] == true -> this
            this.children.hasFocus() -> this.mapChildren(RuntimeNode::skipUnlessFocused)
            else -> SkippedContext(properties, "Skipped $name", parent)
        }
    }


class SkippedContext(
    override val properties: Map<String, Any>,
    override val name: String,
    override val parent: Named?
) : RuntimeContext() {
    override val children = listOf(SkippingTest(this, properties))
}

class SkippingTest(override val parent: Named, override val properties: Map<String, Any>) : RuntimeTest() {
    override val name = "skipped"
    override fun run() {
        throw TestAbortedException("skipped")
    }
}

class SkippedTest(
    override val name: String,
    override val parent: Named?, override val properties: Map<String, Any>
) : RuntimeTest() {
    override fun run() {
        throw TestAbortedException("skipped")
    }
}

private fun RuntimeContext.mapChildren(f: (RuntimeNode) -> RuntimeNode) =
    (this as PreparedRuntimeContext<*, *>).copy(children = children.map(f))

inline fun <reified F> Any.filteredJUnitTests(noinline builder: Context<Unit, F>.() -> Unit): Stream<out DynamicNode> =
    topLevelContext(javaClass.canonicalName, asKType<F>(), builder).filter().focusFilter().toStreamOfDynamicNodes()