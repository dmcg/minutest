package com.oneeyedmen.minutest

import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest

interface MinuTest<in F> {
    val name: String
    val f: F.() -> Any
}

typealias TestDecorator<F> = (t: MinuTest<F>) -> MinuTest<F>

class TestContext<F>(val name: String) {

    private var initialFixtureBuilder: (() -> F)? = null
    private val fixtureTransforms = mutableListOf<(F) -> F>()
    private val tests = mutableListOf<MinuTest<F>>()
    private val contexts = mutableListOf<TestContext<F>>()

    fun fixture(f: () -> F) {
        initialFixtureBuilder = f
    }

    fun modifyFixture(f: F.() -> Unit) {
        fixtureTransforms.add { it.apply(f) }
    }

    fun replaceFixture(f: F.() -> F) {
        fixtureTransforms.add { it.f() }
    }

    fun test(name: String, f: F.() -> Any): SingleTest<F> = SingleTest(name, f).also { tests.add(it) }

    fun context(name: String, f: TestContext<F>.() -> Any): TestContext<F> =
        TestContext<F>(name).apply {
            f()
        }.also { contexts.add(it) }

    fun wrappedWith(decorator: TestDecorator<F>, f: WrapperScope.() -> Any) {
        WrapperScope(decorator).f()
    }

    fun transformedWith(transform: F.() -> F, f: WrapperScope.() -> Any) {
        WrapperScope(transformFixture(transform)).f()
    }

    internal fun build(fixtureBuilder: (() -> F)? = null): DynamicContainer =
        dynamicContainer(name,
            tests.map { test ->
                dynamicTest(test.name) {
                    try {
                        test.f(transformedFeature(fixtureBuilder))
                    } catch (x: ClassCastException) {
                        error("You need to set a fixture by calling fixture(...)")
                    }
                }
            } + contexts.map { it.build { transformedFeature(fixtureBuilder) } }
        )


    @Suppress("UNCHECKED_CAST")
    private fun transformedFeature(initial: (() -> F)?): F {
        val initialFixture = initialFixtureBuilder?.invoke()
            ?: initial?.invoke()
            ?: Unit as F // failures of this case aren't revealed here, but when you actually invoke the test
        return fixtureTransforms.fold(initialFixture) { fixture, transform -> transform(fixture) }
    }

    inner class WrapperScope(private val decorator: TestDecorator<F>) {
        fun test(name: String, f: F.() -> Any): MinuTest<F> = decorator(SingleTest(name, f)).also { tests.add(it) }
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

fun <F> context(f: TestContext<F>.() -> Any): List<DynamicNode> = listOf(TestContext<F>("root").apply { f() }.build())
