package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.internal.MiContext

/**
 * Define a root context to contain tests and other sub-contexts
 */
fun <F> rootContext(name: String, builder: TestContext<F>.() -> Unit): TestContext<F> = MiContext(
    name,
    builder = builder)

/**
 * Represents a test with a name that can be invoked on a fixture.
 */
interface Test<F> : (F) -> F, Node<F>

/**
 * Represents a collection of tests and contexts.
 */
@Suppress("FunctionName")
interface TestContext<F> : Node<F> {

    override val name: String

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

    fun before_(transform: F.() -> F)
    fun before(transform: F.() -> Unit)

    fun after_(transform: F.() -> F)
    fun after(transform: F.() -> Unit)

    fun test_(name: String, f: F.() -> F)
    fun test(name: String, f: F.() -> Unit)

    fun context(name: String, builder: TestContext<F>.() -> Unit): TestContext<F>

    fun addTransform(testTransform: (Test<F>) -> Test<F>)
}

/**
 * Implementation detail.
 */
@Suppress("unused")
interface Node<in F> {
    val name: String
}