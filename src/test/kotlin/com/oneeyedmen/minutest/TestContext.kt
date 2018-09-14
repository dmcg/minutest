package com.oneeyedmen.minutest

import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest

class TestContext<F>(val name: String) {

    @Suppress("UNCHECKED_CAST")
    private var fixtureBuilder: (() -> F) = { Unit as F }
    private val tests = mutableListOf<Pair<String, F.() -> Any>>()
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

    fun test(name: String, f: F.() -> Any) = tests.add(name to f)

    fun context(name: String, f: TestContext<F>.() -> Any) = contexts.add(
        TestContext<F>(
            name).apply {
        fixtureBuilder = this@TestContext.fixtureBuilder
        f()
    })

    internal fun build(): DynamicContainer = dynamicContainer(
        name,
        tests.map { test ->
            dynamicTest(test.first) {
                try {
                    test.second(fixtureBuilder())
                } catch (wrongFixture: ClassCastException) {
                    error("You need to set a fixture by calling fixture(...)")
                }
            }
        } + contexts.map(TestContext<*>::build)
    )

}

fun <F> context(f: TestContext<F>.() -> Any): List<DynamicNode> = listOf(dynamicContainer("root", f))

private fun <T> dynamicContainer(name: String, f: TestContext<T>.() -> Any): DynamicContainer =
    TestContext<T>(name).apply {
        f()
    }.build()
