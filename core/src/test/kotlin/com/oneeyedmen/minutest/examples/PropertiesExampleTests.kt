package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.Named
import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest
import com.oneeyedmen.minutest.internal.PreparedRuntimeContext
import com.oneeyedmen.minutest.internal.asKType
import com.oneeyedmen.minutest.internal.topLevelContext
import com.oneeyedmen.minutest.junit.toStreamOfDynamicNodes
import org.junit.Assert.fail
import org.junit.jupiter.api.TestFactory
import org.opentest4j.TestAbortedException


class PropertiesExampleTests {

    @TestFactory fun skipRoot() = topLevelContext<Unit>(javaClass.canonicalName, asKType<Unit>()) {
        properties["skip"] = true

        test("won't run") {
            fail()
        }
    }.filter().toStreamOfDynamicNodes()

    @TestFactory fun skipContext() = topLevelContext<Unit>(javaClass.canonicalName, asKType<Unit>()) {

        test("will run") {}

        context("skipped") {
            properties["skip"] = true

            test("won't run") {
                fail()
            }
        }

    }.filter().toStreamOfDynamicNodes()

    @TestFactory fun focusContext() = topLevelContext<Unit>(javaClass.canonicalName, asKType<Unit>()) {

        context("focussed") {
            properties["focus"] = true

            test("will run") {}
        }

        context("not focussed") {
            test("won't run") {
                fail()
            }

            context("will run") {
                properties["focus"] = true

                test("will run") {}
            }
        }

        context("another focussed") {
            properties["focus"] = true

            test("will run") {}
        }
    }.focusFilter().toStreamOfDynamicNodes()
}

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
        is RuntimeTest -> false
        is RuntimeContext -> this.properties["focus"] == true || this.children.hasFocus()
    }

private fun RuntimeNode.skipUnlessFocused(): RuntimeNode =
    when (this) {
        is RuntimeTest -> SkippedTest(this.name, this.parent)
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
    override val children = listOf(SkippingTest(this))
}

class SkippingTest(override val parent: Named) : RuntimeTest() {
    override val name = "skipped"
    override fun run() {
        throw TestAbortedException("skipped")
    }
}

class SkippedTest(
    override val name: String,
    override val parent: Named?) : RuntimeTest() {
    override fun run() {
        throw TestAbortedException("skipped")
    }
}

private fun RuntimeContext.mapChildren(f: (RuntimeNode) -> RuntimeNode) =
    (this as PreparedRuntimeContext<*, *>).copy(children = children.map(f))