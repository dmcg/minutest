package com.oneeyedmen.minutest

import org.junit.jupiter.api.DynamicContainer
import org.junit.jupiter.api.DynamicContainer.dynamicContainer
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest

class TestContext<F>(val name: String) {

    private var subjectBuilder: () -> F = { Unit as F }
    private val tests = mutableListOf<Pair<String, F.() -> Any>>()
    private val contexts = mutableListOf<TestContext<F>>()

    fun fixture(f: () -> F) {
        subjectBuilder = f
    }

    fun modifyFixture(f: F.() -> Unit) {
        val oldSubjectBuilder = subjectBuilder
        subjectBuilder = { oldSubjectBuilder().apply(f) }
    }

    fun replaceFixture(f: F.() -> F) {
        val oldSubjectBuilder = subjectBuilder
        subjectBuilder = { oldSubjectBuilder().f() }
    }

    fun test(name: String, f: F.() -> Any) = tests.add(name to f)

    fun context(name: String, f: TestContext<F>.() -> Any) = contexts.add(
        TestContext<F>(
            name).apply {
        subjectBuilder = this@TestContext.subjectBuilder
        f()
    })

    internal fun build(): DynamicContainer = dynamicContainer(
        name,
        tests.map { test ->
            dynamicTest(test.first) {
                test.second(subjectBuilder.invoke())
            }
        } + contexts.map(TestContext<*>::build)
    )
}

fun <F> context(f: TestContext<F>.() -> Any): List<DynamicNode> = listOf(dynamicContainer("root", f))

private fun <T> dynamicContainer(name: String, f: TestContext<T>.() -> Any): DynamicContainer =
    TestContext<T>(name).apply {
        f()
    }.build()
