package com.oneeyedmen.minutest

import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicTest.dynamicTest
import kotlin.streams.asStream

internal class MiContext<F>(
    name: String,
    childTransforms: List<(MinuTest<F>) -> MinuTest<F>> = emptyList(),
    builder: MiContext<F>.() -> Unit
) : TestContext<F>(name){

    private val children = mutableListOf<Node<F>>()
    private val childTransforms = childTransforms.toMutableList()

    init {
        this.builder()
    }

    override fun fixture(factory: () -> F) {
        before_ {
            factory()
        }
    }

    override fun modifyFixture(transform: F.() -> Unit) {
        before(transform)
    }

    override fun replaceFixture(transform: F.() -> F) {
        before_(transform)
    }

    override fun test(name: String, f: F.() -> Unit) = test_(name) {
        apply { f(this) }
    }

    override fun test_(name: String, f: F.() -> F) = MinuTest(name, f).also { children.add(it) }

    override fun context(name: String, builder: TestContext<F>.() -> Unit) =
        MiContext(name, childTransforms, builder).also { children.add(it) }

    override fun addTransform(testTransform: (MinuTest<F>) -> MinuTest<F>) { childTransforms.add(testTransform) }

    internal fun build(): DynamicContainer = dynamicContainer(name,
        children.asSequence().map { dynamicNodeFor(applyTransforms(it)) }.asStream())

    private fun dynamicNodeFor(node: Node<F>) = when (node) {
        is MinuTest<*> -> dynamicNodeFor(node as MinuTest<F>)
        is MiContext<*> -> node.build()
        else -> error("Unexpected test node type")
    }

    @Suppress("UNCHECKED_CAST")
    private fun dynamicNodeFor(test: MinuTest<F>) = dynamicTest(test.name) {
        try {
            test.f(Unit as F)
        } catch (x: ClassCastException) {
            // Provided a fixture has been set, the Unit never makes it as far as any functions that cast it to F, so
            // this works. And if the type of F is Unit, you don't need to set a fixture, as the Unit will do. Simples.
            error("You need to set a fixture by calling fixture(...)")
        }
    }

    private fun applyTransforms(baseNode: Node<F>): Node<F> = childTransforms.reversed().fold(baseNode) { node, transform ->
        when (node) {
            is MinuTest<F> -> transform(node)
            else -> node
        }
    }
}