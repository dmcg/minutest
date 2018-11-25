package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest

val SKIP = Annotation(::skipFilter)

private fun skipFilter(node: RuntimeNode): RuntimeNode = when (node) {
    is RuntimeContext -> skipFilter(node)
    is RuntimeTest -> node
}

private fun skipFilter(context: RuntimeContext): RuntimeNode =
    if (SKIP.appliesTo(context.properties))
        skippingContext(context.properties, "Skipped ${context.name}", context.parent)
    else
        context.mapChildren(::skipFilter)