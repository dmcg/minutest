package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest


val FOCUS = Annotation(::focusFilter)

private fun focusFilter(node: RuntimeNode): RuntimeNode = when (node) {
    is RuntimeContext -> focusFilter(node)
    is RuntimeTest -> node
}

private fun focusFilter(context: RuntimeContext): RuntimeNode =
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
            else -> skippingContext(properties, "Skipped $name", parent)
        }
    }

