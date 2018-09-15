package com.oneeyedmen.minutest

import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicTest.dynamicTest
import kotlin.streams.asStream

internal class MiContext<F>(
    name: String,
    childTransforms: List<(MinuTest<F>) -> MinuTest<F>> = emptyList(),
    builder: MiContext<F>.() -> Any
) : TestContext<F>(name){

    private var initialFixtureBuilder: (() -> F)? = null
    private var fixtureTransform: ((F) -> F)? = null
    private val children = mutableListOf<Node<F>>()
    private val childTransforms = mutableListOf<(MinuTest<F>) -> MinuTest<F>>()

    init {
        this.childTransforms.addAll(childTransforms)
        this.builder()
    }

    override fun fixture(f: () -> F) {
        checkOnlyOneFeatureMod()
        initialFixtureBuilder = f
    }

    override fun modifyFixture(f: F.() -> Unit) {
        checkOnlyOneFeatureMod()
        fixtureTransform = { it.apply(f) }
    }

    override fun replaceFixture(f: F.() -> F) {
        checkOnlyOneFeatureMod()
        fixtureTransform = { it.f() }
    }

    override fun test(name: String, f: F.() -> Any) {
        MinuTest(name, f).also { children.add(it) }
    }

    override fun context(name: String, builder: TestContext<F>.() -> Any) {
        MiContext(name, childTransforms, builder).also { children.add(it) }
    }

    override fun modifyTests(transform: (MinuTest<F>) -> MinuTest<F>) { childTransforms.add(transform) }

    internal fun build(fixtureBuilder: (() -> F)? = null): DynamicContainer = DynamicContainer.dynamicContainer(
        name,
        children.asSequence().map { dynamicNodeFor(applyTransforms(it), fixtureBuilder) }.asStream())

    private fun dynamicNodeFor(node: Node<F>, fixtureBuilder: (() -> F)?) = when (node) {
        is MinuTest<*> -> dynamicNodeFor(node as MinuTest<F>, fixtureBuilder)
        is TestContext<*> -> dynamicNodeFor(node as MiContext<F>, fixtureBuilder)
    }

    private fun dynamicNodeFor(context: MiContext<F>, fixtureBuilder: (() -> F)?) =
        context.build { transformedFeature(fixtureBuilder) }

    private fun dynamicNodeFor(test: MinuTest<F>, fixtureBuilder: (() -> F)?) = dynamicTest(test.name) {
        try {
            test.f(transformedFeature(fixtureBuilder))
        } catch (x: ClassCastException) {
            error("You need to set a fixture by calling fixture(...)")
        }
    }

    private fun applyTransforms(baseNode: Node<F>): Node<F> = childTransforms.fold(baseNode) { node, transform ->
        when (node) {
            is MinuTest<F> -> transform(node)
            else -> node
        }
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