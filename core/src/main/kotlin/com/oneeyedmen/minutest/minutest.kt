package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.internal.asKType
import kotlin.reflect.KType

/**
 * A test with a name that can be invoked on a fixture.
 */
interface Test<F> : (F) -> F {
    val name: String
}

@Suppress("FunctionName")
@MinutestMarker
interface BaseContext<F> {

    val name: String
    val parent: BaseContext<*>?

    /**
     * Define the fixture that will be used in this context's tests and sub-contexts.
     */
    fun fixture(factory: () -> F)

    fun before(block: F.() -> Unit)

    fun after_(transform: F.() -> F)
    fun after(block: F.() -> Unit)

    fun test_(name: String, f: F.() -> F)
    fun test(name: String, f: F.() -> Unit)

    /**
     * Define a sub-context, inheriting the fixture from this.
     */
    fun context(name: String, builder: TestContext<F>.() -> Unit): TestContext<F>

    /**
     * Define a sub-context with a different fixture type.
     */
    fun <F2> derivedContext(
        name: String,
        fixtureType: KType,
        builder: DerivedContext<F, F2>.() -> Unit
    ): DerivedContext<F, F2>

    fun addTransform(testTransform: (Test<F>) -> Test<F>)
}

/**
 * A collection of [Test]s and [TestContext]s.
 */
@Suppress("FunctionName")
interface TestContext<F> : BaseContext<F> {

    /**
     * Modify the parent-context's fixture for use in this context's tests and sub-contexts.
     */
    fun modifyFixture(block: F.() -> Unit)

    /**
     * Replace the parent-context's fixture for use in this context's tests and sub-contexts.
     */
    fun replaceFixture(transform: F.() -> F)
}

@Suppress("FunctionName")
interface DerivedContext<PF, F> : BaseContext<F> {

    /**
     * Replace the parent-context's fixture for use in this context's tests and sub-contexts.
     */
    @Suppress("UNCHECKED_CAST")
    fun deriveFixture(transform: PF.() -> F)
}

/**
 * Define a sub-context with a different fixture type.
 */
inline fun <F, reified F2> TestContext<F>.derivedContext(
    name: String,
    noinline builder: DerivedContext<F, F2>.() -> Unit) {
    this.derivedContext(name, F2::class.asKType(null is F2), builder)
}



