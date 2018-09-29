package com.oneeyedmen.minutest

/**
 * Define a root context to contain tests and other sub-contexts
 */
fun <F> rootContext(name: String, builder: TestContext<F>.() -> Unit): TestContext<F> = MiContext(name, builder = builder)

/**
 * Represents a test with a name that can be invoked on a fixture.
 */
class MinuTest<F>(name: String, val f: F.() -> F) : (F) -> F, Node<F>(name) {
    override fun invoke(fixture: F): F = f(fixture)
}

/**
 * Represents a collection of tests and contexts.
 */
@Suppress("FunctionName")
abstract class TestContext<F>(name: String) : Node<F>(name) {

    /**
     * Define the fixture that will be used in this context's tests and sub-contexts.
     */
    fun fixture(factory: () -> F) {
        before_ {
            factory()
        }
    }

    /**
     * Modify the parent-context's fixture for use in this context's tests and sub-contexts.
     */
    fun modifyFixture(transform: F.() -> Unit) = before(transform)

    /**
     * Replace the parent-context's fixture for use in this context's tests and sub-contexts.
     */
    fun replaceFixture(transform: F.() -> F) = before_(transform)

    abstract fun before_(transform: F.() -> F)
    fun before(transform: F.() -> Unit) = before_ { this.apply(transform) }

    abstract fun after_(transform: F.() -> F)
    fun after(transform: F.() -> Unit) = after_ { this.apply(transform) }

    abstract fun test_(name: String, f: F.() -> F): MinuTest<F>
    fun test(name: String, f: F.() -> Unit) = test_(name) {
        apply { f(this) }
    }

    abstract fun context(name: String, builder: TestContext<F>.() -> Unit): TestContext<F>

    abstract fun addTransform(testTransform: (MinuTest<F>) -> MinuTest<F>)
}

/**
 * Implementation detail.
 */
@Suppress("unused")
sealed class Node<in F>(val name: String)
