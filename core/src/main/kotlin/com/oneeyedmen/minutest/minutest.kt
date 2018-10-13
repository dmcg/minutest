package com.oneeyedmen.minutest

/**
 * A test with a name that can be invoked on a fixture.
 */
interface Test<F: Any> : (F) -> F {
    val name: String
}

/**
 * A collection of [Test]s and [TestContext]s.
 */
@Suppress("FunctionName")
interface TestContext<F: Any> {

    val name: String

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
