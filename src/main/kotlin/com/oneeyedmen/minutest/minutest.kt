package com.oneeyedmen.minutest

import org.junit.jupiter.api.DynamicNode

fun <F> context(builder: TestContext<F>.() -> Any): List<DynamicNode> = listOf(
    MiContext("root", builder = builder).build()
)

sealed class Node<in F>(val name: String)

class MinuTest<F>(name: String, val f: F.() -> Any) : Node<F>(name)

abstract class TestContext<F>(name: String): Node<F>(name) {
    abstract fun fixture(f: () -> F)
    abstract fun modifyFixture(f: F.() -> Unit)
    abstract fun replaceFixture(f: F.() -> F)
    abstract fun test(name: String, f: F.() -> Any): MinuTest<F>
    abstract fun context(name: String, builder: TestContext<F>.() -> Any): TestContext<F>
    abstract fun modifyTests(transform: (MinuTest<F>) -> MinuTest<F>)
}

fun <F> TestContext<F>.before(transform: F.() -> Any) = modifyTests { aroundTest(it, before = transform) }

fun <F> TestContext<F>.after(transform: F.() -> Any) = modifyTests { aroundTest(it, after = transform) }

fun <F> aroundTest(test: MinuTest<F>, before: F.() -> Any = {}, after: F.() -> Any = {}) = MinuTest<F>(test.name) {
    before(this)
    test.f(this)
    after(this)
}
