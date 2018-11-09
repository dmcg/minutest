package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.internal.MinutestMarker
import com.oneeyedmen.minutest.internal.asKType
import kotlin.reflect.KType

typealias TestContext<F> = Context<*, F>

@MinutestMarker
abstract class Context<ParentF, F> {

    /**
     * Define the fixture that will be used in this context's tests and sub-contexts.
     *
     * Has access to the parent context's fixture as 'this'
     */
    @Suppress("FunctionName")
    @Deprecated("Replace with deriveFixture")
    fun fixture_(factory: ParentF.(testDescriptor: TestDescriptor) -> F) = deriveFixture(factory)

    /**
     * Define the fixture that will be used in this context's tests and sub-contexts by transforming the parent fixture.
     */
    @Deprecated("Replace with deriveFixture")
    fun mapFixture(f: (parentFixture: ParentF) -> F) = deriveFixture { f(this) }

    /**
     * Define the fixture that will be used in this context's tests and sub-contexts by transforming the parent fixture,
     * (as 'this').
     */
    abstract fun deriveFixture(f: (ParentF).(testDescriptor: TestDescriptor) -> F)

    /**
     * Define the fixture that will be used in this context's tests and sub-contexts.
     */
    fun fixture(factory: (testDescriptor: TestDescriptor) -> F) = deriveFixture { factory(it) }

    /**
     * Apply an operation to the current fixture (accessible as 'this') before running tests or sub-contexts.
     */
    fun modifyFixture(operation: F.() -> Unit) = before(operation)

    /**
     * Apply an operation to the current fixture (accessible as 'this') before running tests or sub-contexts.
     */
    abstract fun before(operation: F.() -> Unit)

    /**
     * Apply an operation to the current fixture (accessible as 'this') after running tests.
     *
     * Will be invoked even if tests or 'befores' throw exceptions.
     *
     * An exception thrown in an after will prevent later afters running.
     */
    abstract fun after(operation: F.() -> Unit)

    /**
     * Define a test on the current fixture (accessible as 'this').
     */
    fun test(name: String, f: F.() -> Unit) = test_(name) { this.apply(f) }

    /**
     * Define a test on the current fixture (accessible as 'this'), returning a new fixture to be processed by 'afters'.
     */
    @Suppress("FunctionName")
    abstract fun test_(name: String, f: F.() -> F)

    /**
     * Define a sub-context, inheriting the fixture from this.
     */
    abstract fun context(name: String, builder: Context<F, F>.() -> Unit)

    /**
     * Define a sub-context with a different fixture type.
     *
     * You will have to call [fixture]' in the sub-context to convert from the parent to the child fixture type.
     */
    inline fun <reified G> derivedContext(name: String, noinline builder: Context<F, G>.() -> Unit) {
        createSubContext(name, asKType<G>(), null, false, builder)
    }

    /**
     * Define a sub-context with a different fixture type, supplying the new fixture value
     */
    inline fun <reified G> derivedContext(name: String, fixture: G, noinline builder: Context<F, G>.() -> Unit) {
        createSubContext(name, asKType<G>(), { fixture }, true, builder)
    }

    /**
     * Define a sub-context with a different fixture type, supplying a fixture converter.
     */
    inline fun <reified G> derivedContext(name: String,
        noinline fixtureFactory: F.(TestDescriptor) -> G,
        noinline builder: Context<F, G>.() -> Unit
    ) {
        createSubContext(name, asKType<G>(), fixtureFactory, true, builder)
    }

    /**
     * Add a transform to be applied to the tests
     */
    abstract fun addTransform(transform: TestTransform<F>)

    abstract fun <G> createSubContext(
        name: String,
        type: KType,
        fixtureFactory: (F.(TestDescriptor) -> G)?,
        explicitFixtureFactory: Boolean,
        builder: Context<F, G>.() -> Unit
    )


    val F.it get()= this

    val ParentF.parentFixture get() = this
    val F.fixture get() = this
}
