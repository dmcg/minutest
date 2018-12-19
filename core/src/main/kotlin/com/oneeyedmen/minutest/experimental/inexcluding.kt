package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.*
import org.opentest4j.TestAbortedException


object SKIP : TestAnnotation
object FOCUS : TestAnnotation

val skipAndFocus = ::inexclude

fun inexclude(root: RuntimeNode) = when (root) {
    is RuntimeTest -> root.inexcluded(defaultToSkip = false)
    is RuntimeContext -> {
        if (SKIP.appliesTo(root)) {
            root.skipped()
        } else {
            val defaultToSkip = root.hasAFocusedChild()
            root.withTransformedChildren { it.inexcluded(defaultToSkip) }
        }
    }
}

private fun RuntimeNode.hasAFocusedChild(): Boolean = when (this) {
    is RuntimeTest -> FOCUS.appliesTo(this)
    is RuntimeContext -> hasAFocusedChild()
}

private fun RuntimeContext.hasAFocusedChild() = FOCUS.appliesTo(this) || children.hasAFocusedChild()

private fun List<RuntimeNode>.hasAFocusedChild() = find { it.hasAFocusedChild() } != null

private fun RuntimeNode.inexcluded(defaultToSkip: Boolean): RuntimeNode = when (this) {
    is RuntimeTest -> inexcluded(defaultToSkip)
    is RuntimeContext -> inexcluded(defaultToSkip)
}

private fun RuntimeContext.inexcluded(defaultToSkip: Boolean) =
    when {
        FOCUS.appliesTo(this) ->
            this.withTransformedChildren { it.inexcluded(defaultToSkip = false) }
        hasAFocusedChild() ->
            this.withTransformedChildren { it.inexcluded(defaultToSkip) }
        defaultToSkip || SKIP.appliesTo(this) ->
            this.skipped()
        else ->
            this.withTransformedChildren { it.inexcluded(defaultToSkip) }
    }

private fun RuntimeTest.inexcluded(defaultToSkip: Boolean) =
    when {
        FOCUS.appliesTo(this) -> this
        defaultToSkip || SKIP.appliesTo(this) -> this.skipped()
        else -> this
    }

private fun RuntimeTest.skipped() = skipper(name, properties)

private fun skipper(name: String,
    properties: Map<Any, Any>
): LoadedRuntimeTest = LoadedRuntimeTest(name, properties,
    xRunner = { throw TestAbortedException("skipped") })

private fun RuntimeContext.skipped() = LoadedRuntimeContext(this,
    children = listOf(skipper("skipping ${this.name}", emptyMap()))
)
