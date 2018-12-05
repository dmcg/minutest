package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.experimental.loggedTo
import com.oneeyedmen.minutest.experimental.withTabsExpanded
import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Test
import org.opentest4j.TestAbortedException


class IncludeExcludeTests {

    @Test fun noop() {
        val log = mutableListOf<String>()
        val tests = junitTests<Unit>(::inexclude.then(loggedTo(log))) {

            test("t1") {}
            test("t2") {}
        }
        executeTests(tests)
        assertLogged(log.withTabsExpanded(4),
            "com.oneeyedmen.minutest.IncludeExcludeExampleTests",
            "    t1",
            "    t2"
        )
    }

    @Test fun `exclude test`() {
        val log = mutableListOf<String>()
        val tests = junitTests<Unit>(::inexclude.then(loggedTo(log))) {
            EXCLUDE - test("t1") {}
            test("t2") {}
        }
        executeTests(tests)
        assertLogged(log.withTabsExpanded(4),
            "com.oneeyedmen.minutest.IncludeExcludeExampleTests",
            "    t1 skipped",
            "    t2"
        )
    }

    @Test fun `exclude context`() {
        val log = mutableListOf<String>()
        val tests = junitTests<Unit>(::inexclude.then(loggedTo(log))) {
            EXCLUDE - context("c1") {
                test("c1t1") {}
            }
            test("t2") {}
        }
        executeTests(tests)
        assertLogged(log.withTabsExpanded(4),
            "com.oneeyedmen.minutest.IncludeExcludeExampleTests",
            "    c1 skipped",
            "    t2"
        )
    }

    @Test fun `include test skips not included`() {
        val log = mutableListOf<String>()
        val tests = junitTests<Unit>(::inexclude.then(loggedTo(log))) {
            test("t1") {}
            INCLUDE - test("t2") {}
        }
        executeTests(tests)
        assertLogged(log.withTabsExpanded(4),
            "com.oneeyedmen.minutest.IncludeExcludeExampleTests",
            "    t1 skipped",
            "    t2"
        )
    }

    @Test fun `include context skips no included`() {
        val log = mutableListOf<String>()
        val tests = junitTests<Unit>(::inexclude.then(loggedTo(log))) {
            test("t1") {}
            INCLUDE - context("c1") {
                test("c1t1") {}
            }
        }
        executeTests(tests)
        assertLogged(log.withTabsExpanded(4),
            "com.oneeyedmen.minutest.IncludeExcludeExampleTests",
            "    t1 skipped",
            "    c1",
            "        c1t1"
        )
    }
}

private fun ((RuntimeNode) -> RuntimeNode).then(next: (RuntimeNode) -> RuntimeNode) = { node: RuntimeNode ->
    next(this(node))
}


fun inexclude(node: RuntimeNode): RuntimeNode = when (node) {
    is RuntimeTest -> node.inexclude()
    is RuntimeContext -> node.inexclude()
}

private fun RuntimeContext.inexclude() =
    when {
        this.properties.containsKey(EXCLUDE) -> this.skipped()
        this.children.find { it.properties.containsKey(INCLUDE) } != null ->
            this.withChildren(withoutSpecificallyIncluded())
        else -> this.withChildren(this.children.map { inexclude(it) })
    }

private fun RuntimeContext.withoutSpecificallyIncluded() = children.map {
    if (it.properties.containsKey(INCLUDE)) it else it.skipped()
}

private fun RuntimeTest.inexclude() =
    if (this.properties.containsKey(EXCLUDE))
        this.skipped()
    else
        this

private fun RuntimeNode.skipped() = when(this) {
    is RuntimeContext -> this.skipped()
    is RuntimeTest -> this.skipped()
}

private fun RuntimeTest.skipped() = object : RuntimeTest() {
    override val name = "${this@skipped.name} skipped"
    override val properties = this@skipped.properties
    override val parent = this@skipped.parent
    override fun run() {
        throw TestAbortedException("skipped")
    }
}

private fun RuntimeContext.skipped() = object : RuntimeTest() {
    override val name = "${this@skipped.name} skipped"
    override val properties = this@skipped.properties
    override val parent = this@skipped.parent
    override fun run() {
        throw TestAbortedException("skipped")
    }
}

val EXCLUDE = Annotation()
val INCLUDE = Annotation()

class Annotation {
    fun applyTo(properties: MutableMap<Any, Any>) {
        properties[this] = true
    }
}

operator fun <F> Annotation.minus(nodeBuilder: NodeBuilder<F>): NodeBuilder<F> =
    nodeBuilder.also {
        this.applyTo(it.properties)
    }

