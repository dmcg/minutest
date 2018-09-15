package com.oneeyedmen.minutest

import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import kotlin.streams.asStream

interface TestNode<in F> {
    val name: String
}

interface MinuTest<in F> : TestNode<F> {
    override val name: String
    val f: F.() -> Any
}

typealias TestDecorator<F> = (t: MinuTest<F>) -> MinuTest<F>

class TestContext<F>(override val name: String, builder: TestContext<F>.() -> Any) : TestNode<F>{

    private var initialFixtureBuilder: (() -> F)? = null
    private val fixtureTransforms = mutableListOf<(F) -> F>()
    private val children = mutableListOf<TestNode<F>>()

    init {
        this.builder()
    }

    fun fixture(f: () -> F) {
        initialFixtureBuilder = f
    }

    fun modifyFixture(f: F.() -> Unit) {
        fixtureTransforms.add { it.apply(f) }
    }

    fun replaceFixture(f: F.() -> F) {
        fixtureTransforms.add { it.f() }
    }

    fun test(name: String, f: F.() -> Any): MinuTest<F> = SingleTest(name, f).also { children.add(it) }

    fun context(name: String, builder: TestContext<F>.() -> Any): TestContext<F> =
        TestContext<F>(name, builder).also { children.add(it) }

    fun wrappedWith(decorator: TestDecorator<F>, f: WrapperScope.() -> Any) {
        WrapperScope(decorator).f()
    }

    fun transformedWith(transform: F.() -> F, f: WrapperScope.() -> Any) {
        WrapperScope(transformFixture(transform)).f()
    }

    internal fun build(fixtureBuilder: (() -> F)? = null): DynamicContainer =
        dynamicContainer(name, children.asSequence().map { dynamicNodeFor(it, fixtureBuilder) }.asStream())

    private fun dynamicNodeFor(node: TestNode<F>, fixtureBuilder: (() -> F)?) = when (node) {
        is MinuTest<*> -> dynamicNodeFor(node as MinuTest<F>, fixtureBuilder)
        is TestContext<*> -> dynamicNodeFor(node as TestContext<F>, fixtureBuilder)
        else -> error("Unexpected node type")
    }

    private fun dynamicNodeFor(testContext: TestContext<F>, fixtureBuilder: (() -> F)?) =
        testContext.build { transformedFeature(fixtureBuilder) }

    private fun dynamicNodeFor(test: MinuTest<F>, fixtureBuilder: (() -> F)?) = dynamicTest(test.name) {
        try {
            test.f(transformedFeature(fixtureBuilder))
        } catch (x: ClassCastException) {
            error("You need to set a fixture by calling fixture(...)")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun transformedFeature(initial: (() -> F)?): F {
        val initialFixture = initialFixtureBuilder?.invoke()
            ?: initial?.invoke()
            ?: Unit as F // failures of this case aren't revealed here, but when you actually invoke the test
        return fixtureTransforms.fold(initialFixture) { fixture, transform -> transform(fixture) }
    }

    inner class WrapperScope(private val decorator: TestDecorator<F>) {
        fun test(name: String, f: F.() -> Any): MinuTest<F> = decorator(SingleTest(name, f)).also { children.add(it) }
    }
}

class SingleTest<F>(
    override val name: String,
    override val f: F.() -> Any
) : MinuTest<F>

fun <F> transformFixture(transform: F.() -> F) = fun(t: MinuTest<F>): MinuTest<F> = SingleTest(t.name) {
    t.f(transform(this))
}

@Suppress("UNCHECKED_CAST")
fun <F> skipTest() = skipTest as TestDecorator<F>

private val skipTest = object : TestDecorator<Any> {
    override fun invoke(t: MinuTest<Any>): MinuTest<Any> = SingleTest(t.name) {}
}

fun <F> context(builder: TestContext<F>.() -> Any): List<DynamicNode> = listOf(TestContext("root", builder).build())

