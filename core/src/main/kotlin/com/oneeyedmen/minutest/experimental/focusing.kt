package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest


object FOCUS : Annotation<FOCUS>() {
    override val transform = FocusInterpreter
}

object FocusInterpreter : AnnotationInterpreter<FOCUS>(FOCUS::class) {
    override fun defaultAnnotationValue() = FOCUS
    override fun invoke(node: RuntimeNode) = focusFilter(node)
    
    private fun focusFilter(node: RuntimeNode): RuntimeNode = when (node) {
        is RuntimeContext -> focusFilter(node)
        is RuntimeTest -> node
    }
    
    private fun focusFilter(context: RuntimeContext): RuntimeNode =
        if (context.children.hasFocus())
            context.mapChildren { it.skipUnlessFocused() }
        else context
    
    private fun Iterable<RuntimeNode>.hasFocus(): Boolean = this.find { it.hasFocus() } != null
    
    private fun RuntimeNode.hasFocus() =
        when (this) {
            is RuntimeTest -> appliesTo(this)
            is RuntimeContext -> appliesTo(this) || this.children.hasFocus()
        }
    
    private fun RuntimeNode.skipUnlessFocused(): RuntimeNode =
        when (this) {
            is RuntimeTest -> when {
                appliesTo(this) -> this
                else -> SkippedTest(this.name, this.parent, properties)
            }
            is RuntimeContext -> when {
                appliesTo(this) -> this
                this.children.hasFocus() -> this.mapChildren { it.skipUnlessFocused() }
                else -> skippingContext(properties, "Skipped $name", parent)
            }
        }
    
}

