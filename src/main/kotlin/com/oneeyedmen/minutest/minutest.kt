package com.oneeyedmen.minutest

import org.junit.jupiter.api.DynamicNode

fun <F> context(builder: TestContext<F>.() -> Unit): List<DynamicNode> = listOf(
    MiContext("root", builder = builder).build()
)

sealed class Node<in F>(val name: String)

class MinuTest<F>(name: String, val f: F.() -> Unit): (F) -> Unit, Node<F>(name) {
    override fun invoke(fixture: F): Unit = f(fixture)
}

abstract class TestContext<F>(name: String): Node<F>(name) {
    abstract fun fixture(factory: () -> F)
    abstract fun modifyFixture(transform: F.() -> Unit)
    abstract fun replaceFixture(transform: F.() -> F)
    abstract fun test(name: String, f: F.() -> Unit): MinuTest<F>
    abstract fun context(name: String, builder: TestContext<F>.() -> Unit): TestContext<F>
    abstract fun modifyTests(transform: (MinuTest<F>) -> MinuTest<F>)
}

fun <F> TestContext<F>.before(transform: F.() -> Unit) = modifyTests { aroundTest(it, before = transform) }

fun <F> TestContext<F>.after(transform: F.() -> Unit) = modifyTests { aroundTest(it, after = transform) }

fun <F> aroundTest(test: MinuTest<F>, before: F.() -> Unit = {}, after: F.() -> Unit = {}) = MinuTest<F>(test.name) {
    before(this)
    test.f(this)
    after(this)
}
