package com.oneeyedmen.minutest

import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import kotlin.streams.asStream

sealed class Node<in F>(val name: String)

abstract class MinuTest<in F>(name: String) : Node<F>(name) {
    abstract val f: F.() -> Any
}

class SingleTest<F>(name: String, override val f: F.() -> Any) : MinuTest<F>(name)

class Context<F>(
    name: String,
    childTransforms: List<(Node<F>) -> Node<F>> = emptyList(),
    builder: Context<F>.() -> Any
) : Node<F>(name){

    private var initialFixtureBuilder: (() -> F)? = null
    private var fixtureTransform: ((F) -> F)? = null
    private val children = mutableListOf<Node<F>>()
    private val childTransforms = mutableListOf<(Node<F>) -> Node<F>>()

    init {
        this.childTransforms.addAll(childTransforms)
        this.builder()
    }

    fun fixture(f: () -> F) {
        checkOnlyOneFeatureMod()
        initialFixtureBuilder = f
    }

    fun modifyFixture(f: F.() -> Unit) {
        checkOnlyOneFeatureMod()
        fixtureTransform = { it.apply(f) }
    }

    fun replaceFixture(f: F.() -> F) {
        checkOnlyOneFeatureMod()
        fixtureTransform = { it.f() }
    }

    fun test(name: String, f: F.() -> Any): MinuTest<F> = SingleTest(name, f).also { children.add(it) }

    fun context(name: String, builder: Context<F>.() -> Any): Context<F> =
        Context(name, childTransforms, builder).also { children.add(it) }

    fun modifyTests(transform: (Node<F>) -> Node<F>) { childTransforms.add(transform) }

    internal fun build(fixtureBuilder: (() -> F)? = null): DynamicContainer = dynamicContainer(name,
        children.asSequence().map { dynamicNodeFor(applyTransforms(it), fixtureBuilder) }.asStream())

    private fun dynamicNodeFor(node: Node<F>, fixtureBuilder: (() -> F)?) = when (node) {
        is MinuTest<*> -> dynamicNodeFor(node as MinuTest<F>, fixtureBuilder)
        is Context<*> -> dynamicNodeFor(node as Context<F>, fixtureBuilder)
    }

    private fun dynamicNodeFor(context: Context<F>, fixtureBuilder: (() -> F)?) =
        context.build { transformedFeature(fixtureBuilder) }

    private fun dynamicNodeFor(test: MinuTest<F>, fixtureBuilder: (() -> F)?) = dynamicTest(test.name) {
        try {
            test.f(transformedFeature(fixtureBuilder))
        } catch (x: ClassCastException) {
            error("You need to set a fixture by calling fixture(...)")
        }
    }

    private fun applyTransforms(baseNode: Node<F>): Node<F> = childTransforms.fold(baseNode) { acc, transform ->
        transform(acc)
    }

    @Suppress("UNCHECKED_CAST")
    private fun transformedFeature(initial: (() -> F)?): F {
        val initialFixture = initialFixtureBuilder?.invoke()
            ?: initial?.invoke()
            ?: Unit as F // failures of this case aren't revealed here, but when you actually invoke the test
        return fixtureTransform?.let { it(initialFixture) } ?: initialFixture
    }

    private fun checkOnlyOneFeatureMod() {
        if (initialFixtureBuilder != null || fixtureTransform != null)
            error("This context already has its fixture set")
    }
}

fun <F> Context<F>.before(transform: F.() -> Any) = modifyTests { node ->
    when (node) {
        is MinuTest<F> -> aroundTest(node, before = transform)
        is Context<F> -> node
    }
}

fun <F> Context<F>.after(transform: F.() -> Any) = modifyTests { node ->
    when (node) {
        is MinuTest<F> -> aroundTest(node, after = transform)
        is Context<F> -> node
    }
}
fun <F> aroundTest(test: MinuTest<F>, before: F.() -> Any = {}, after: F.() -> Any = {}) = SingleTest<F>(test.name) {
    before(this)
    test.f(this)
    after(this)
}


fun <F> context(builder: Context<F>.() -> Any): List<DynamicNode> = listOf(Context("root", builder = builder).build())

