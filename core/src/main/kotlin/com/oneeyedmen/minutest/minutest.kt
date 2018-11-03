package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.internal.MinutestMarker

typealias TestContext<F> = Context<*, F>

@MinutestMarker
interface Context<ParentF, F> {
    
    /**
     * Define the fixture that will be used in this context's tests and sub-contexts.
     *
     * Has access to the parent context's fixture as 'this'
     */
    fun fixture(factory: ParentF.() -> F)
    
    /**
     * Apply an operation to the current fixture (accessible as 'this') before running tests or sub-contexts.
     */
    fun modifyFixture(operation: F.() -> Unit) = before(operation)
    
    /**
     * Apply an operation to the current fixture (accessible as 'this') before running tests or sub-contexts.
     */
    fun before(operation: F.() -> Unit)
    
    /**
     * Apply an operation to the current fixture (accessible as 'this') after running tests.
     *
     * Will be invoked even if tests or 'befores' throw exceptions.
     *
     * An exception thrown in an after will prevent later afters running.
     */
    fun after(operation: F.() -> Unit)
    
    /**
     * Define a test on the current fixture (accessible as 'this').
     */
    fun test(name: String, f: F.() -> Unit)
    
    /**
     * Define a test on the current fixture (accessible as 'this'), returning a new fixture to be processed by 'afters'.
     */
    @Suppress("FunctionName")
    fun test_(name: String, f: F.() -> F)
    
    /**
     * Define a sub-context, inheriting the fixture from this.
     */
    fun context(name: String, builder: Context<F, F>.() -> Unit)
    
    /**
     * Define a sub-context with a different fixture type.
     *
     * You will have to call [fixture]' in the sub-context to convert from the parent to the child fixture type.
     */
    fun <G> derivedContext(name: String, builder: Context<F, G>.() -> Unit)

    /**
     * Define a sub-context with a different fixture type, supplying the new fixture value
     */
    fun <G> derivedContext(name: String, fixture: G, builder: Context<F, G>.() -> Unit)

    /**
     * Define a sub-context with a different fixture type, supplying a fixture converter.
     */
    fun <G> derivedContext(name: String, fixtureFactory: F.() -> G, builder: Context<F, G>.() -> Unit)

    /**
     * Add a transform to be applied to the tests
     */
    fun addTransform(transform: TestTransform<F>)
}

