package com.oneeyedmen.minutest

fun <F> context(builder: TestContext<F>.() -> Unit) =
    MiContext("ignored", builder = builder).build().children

sealed class Node<in F>(val name: String)

class MinuTest<F>(name: String, val f: F.() -> F): (F) -> F, Node<F>(name) {
    override fun invoke(fixture: F): F = f(fixture)
}

abstract class TestContext<F>(name: String): Node<F>(name) {
    abstract fun fixture(factory: () -> F)
    abstract fun modifyFixture(transform: F.() -> Unit)
    abstract fun replaceFixture(transform: F.() -> F)
    abstract fun test(name: String, f: F.() -> Unit): MinuTest<F>
    abstract fun test_(name: String, f: F.() -> F): MinuTest<F>
    abstract fun context(name: String, builder: TestContext<F>.() -> Unit): TestContext<F>
    abstract fun modifyTests(testTransform: (MinuTest<F>) -> MinuTest<F>)
}

fun <F> TestContext<F>.before(transform: F.() -> Unit) = before_ { apply(transform) }

fun <F> TestContext<F>.before_(transform: F.() -> F) = modifyTests {
    aroundTest(it, before = transform)
}

fun <F> TestContext<F>.after(transform: F.() -> Unit) = after_ { apply(transform) }
fun <F> TestContext<F>.after_(transform: F.() -> F) = modifyTests {
    aroundTest(it, after = transform)
}

fun <F> aroundTest(
    test: MinuTest<F>,
    before: F.() -> F = { this },
    after: F.() -> F = { this }
) = MinuTest<F>(test.name) {
    test.f(this.before()).after()
}
