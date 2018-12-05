package com.oneeyedmen.minutest.experimental

import com.oneeyedmen.minutest.RuntimeContext
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.RuntimeTest

fun checkedAgainst(check: (List<String>) -> Unit): (RuntimeNode) -> RuntimeNode = { node ->
    when (node) {
        is RuntimeTest -> error("Can only check a context")
        is RuntimeContext -> {
            val log = mutableListOf<String>()
            LoggingRuntimeContext(node, log, 0) {
                check(log)
            }
        }
    }
}

fun loggedTo(log: MutableList<String>): (RuntimeNode) -> RuntimeNode = { node ->
    node.loggedTo(log, 0)
}

fun List<String>.withTabsExpanded(spaces: Int) = this.map { it.replace("\t", " ".repeat(spaces))}

private fun RuntimeNode.loggedTo(log: MutableList<String>, level: Int): RuntimeNode =
    when (this) {
        is RuntimeContext -> LoggingRuntimeContext(this, log, level)
        is RuntimeTest -> LoggingRuntimeTest(this, log, level)
    }

private data class LoggingRuntimeContext(
    private val wrapped: RuntimeContext,
    private val log: MutableList<String>,
    private val indent: Int,
    private val done: () -> Unit = {}
) : RuntimeContext() {

    private val childrenLog = mutableListOf<String>()
    override val properties = wrapped.properties
    override val children = wrapped.children.map { it.loggedTo(childrenLog, indent + 1) }
    override val name = wrapped.name
    override val parent = wrapped.parent

    override fun withChildren(children: List<RuntimeNode>) = this.copy(wrapped = wrapped.withChildren(children))

    override fun close() {
        log.add("${indent.tabs()}$name")
        log.addAll(childrenLog)
        wrapped.close()
        done()
    }
}

private data class LoggingRuntimeTest(
    private val wrapped: RuntimeTest,
    private val log: MutableList<String>,
    private val indent: Int
) : RuntimeTest() {

    override val properties = wrapped.properties
    override val name = wrapped.name
    override val parent = wrapped.parent

    override fun run() {
        log.add("${indent.tabs()}$name")
        wrapped.run()
    }
}

private fun Int.tabs() = "\t".repeat(this)