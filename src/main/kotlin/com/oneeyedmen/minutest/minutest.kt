package com.oneeyedmen.minutest

fun <F> rootContext(builder: TestContext<F>.() -> Unit): TestContext<F> = MiContext("ignored", builder = builder)

sealed class Node<in F>(val name: String)

class MinuTest<F>(name: String, val f: F.() -> F) : (F) -> F, Node<F>(name) {
    override fun invoke(fixture: F): F = f(fixture)
}

abstract class TestContext<F>(name: String) : Node<F>(name) {
    abstract fun fixture(factory: () -> F)
    abstract fun modifyFixture(transform: F.() -> Unit)
    abstract fun replaceFixture(transform: F.() -> F)
    abstract fun test(name: String, f: F.() -> Unit): MinuTest<F>
    abstract fun test_(name: String, f: F.() -> F): MinuTest<F>
    abstract fun context(name: String, builder: TestContext<F>.() -> Unit): TestContext<F>
    abstract fun addTransform(testTransform: (MinuTest<F>) -> MinuTest<F>)
    internal abstract val operations: Operations<F>
}

fun <F> TestContext<F>.before(transform: F.() -> Unit) = before_ { this.apply(transform) }

fun <F> TestContext<F>.before_(transform: F.() -> F) {
    operations.befores.add(transform)
}

fun <F> TestContext<F>.after(transform: F.() -> Unit) = after_ { this.apply(transform) }

fun <F> TestContext<F>.after_(transform: F.() -> F) {
    operations.afters.add(transform)
}