package com.oneeyedmen.minutest

import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest

interface MinuTest<in F> {
    val name: String
    val f: F.() -> Any
}

typealias Decorator<F> = (t: MinuTest<F>) -> MinuTest<F>

class TestContext<F>(val name: String) {

    @Suppress("UNCHECKED_CAST")
    private var fixtureBuilder: (() -> F) = { Unit as F }
    private val tests = mutableListOf<MinuTest<F>>()
    private val contexts = mutableListOf<TestContext<F>>()

    fun fixture(f: () -> F) {
        fixtureBuilder = f
    }

    fun modifyFixture(f: F.() -> Unit) {
        val inheritedFixtureBuilder = fixtureBuilder
        fixtureBuilder = { inheritedFixtureBuilder().apply(f) }
    }

    fun replaceFixture(f: F.() -> F) {
        val inheritedFixtureBuilder = fixtureBuilder
        fixtureBuilder = { inheritedFixtureBuilder().f() }
    }

    fun test(name: String, f: F.() -> Any): SingleTest<F> = SingleTest(name, f).also { tests.add(it) }

    fun context(name: String, f: TestContext<F>.() -> Any): TestContext<F> =
        TestContext<F>(name).apply {
            fixtureBuilder = this@TestContext.fixtureBuilder
            f()
        }.also { contexts.add(it) }

    fun wrapped(decorator: Decorator<F>, f: WrapperScope.() -> Any) {
        WrapperScope(decorator).f()
    }

    internal fun build(): DynamicContainer = dynamicContainer(
        name,
        tests.map { test ->
            dynamicTest(test.name) {
                try {
                    test.f(fixtureBuilder())
                } catch (wrongFixture: ClassCastException) {
                    error("You need to set a fixture by calling fixture(...)")
                }
            }
        } + contexts.map(TestContext<*>::build)
    )

    inner class WrapperScope(private val decorator: Decorator<F>) {
        fun test(name: String, f: F.() -> Any): MinuTest<F> = decorator(SingleTest(name, f)).also { tests.add(it) }
    }
}

class SingleTest<F>(
    override val name: String,
    override val f: F.() -> Any
) : MinuTest<F>

fun <F> context(f: TestContext<F>.() -> Any): List<DynamicNode> = listOf(
    dynamicContainer(
        "root",
        f))

private fun <T> dynamicContainer(name: String, f: TestContext<T>.() -> Any): DynamicContainer =
    TestContext<T>(name).apply {
        f()
    }.build()
