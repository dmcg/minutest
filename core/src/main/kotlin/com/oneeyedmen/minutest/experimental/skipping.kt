package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest

object SKIP : Annotation<SKIP>() {
    override val transform = SkipInterpreter
}

object SkipInterpreter : AnnotationInterpreter<SKIP>(SKIP::class) {
    override fun defaultAnnotationValue() = SKIP
    
    override fun invoke(node: RuntimeNode) = skipFilter(node)
    
    private fun skipFilter(node: RuntimeNode): RuntimeNode = when (node) {
        is RuntimeContext -> skipFilter(node)
        is RuntimeTest -> node
    }
    
    private fun skipFilter(context: RuntimeContext): RuntimeNode =
        if (appliesTo(context))
            skippingContext(context.properties, "Skipped ${context.name}", context.parent)
        else
            context.mapChildren(::skipFilter)
}

