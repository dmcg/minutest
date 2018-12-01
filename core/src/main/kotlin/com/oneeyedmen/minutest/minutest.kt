package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.internal.FixtureType
import com.oneeyedmen.minutest.internal.MinutestMarker
import com.oneeyedmen.minutest.internal.askType

typealias TestContext<F> = Context<*, F>

@MinutestMarker
abstract class Context<ParentF, F> {

    /**
     * Define a child-context, inheriting the fixture from the parent.
     */
    abstract fun context(name: String, builder: Context<F, F>.() -> Unit): NodeBuilder<F>

    /**
     * Define a child-context with a different fixture type.
     *
     * You will have to call [deriveFixture] in the sub-context to convert from the parent
     * to the child fixture type.
     */
    inline fun <reified G> derivedContext(name: String, noinline builder: Context<F, G>.() -> Unit) =
        internalCreateContext(name, askType<G>(), null, builder)

    /**
     * Define the fixture that will be used in this context's tests and sub-contexts.
     */
    fun fixture(factory: (Unit).() -> F) = deriveFixture { Unit.factory() }

    /**
     * Apply an operation to the current fixture (accessible as the receiver 'this')
     * before running tests or sub-contexts.
     */
    fun modifyFixture(operation: F.() -> Unit) = before(operation)

    /**
     * Define the fixture that will be used in this context's tests and sub-contexts by
     * transforming the parent fixture, accessible as the receiver 'this'.
     */
    abstract fun deriveFixture(f: (ParentF).() -> F)

    /**
     * Define a test on the current fixture (accessible as 'this').
     */
    fun test(name: String, f: F.() -> Unit) = test_(name) { this.apply(f) }

    /**
     * Define a test on the current fixture (accessible as the receiver 'this'), returning
     * a new fixture to be processed by 'afters'.
     */
    @Suppress("FunctionName")
    abstract fun test_(name: String, f: F.() -> F): NodeBuilder<F>

    /**
     * Apply an operation to the current fixture (accessible as the receiver 'this') before
     * running tests or sub-contexts.
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
     * Make the fixture available as 'it' to improve communication in tests.
     */
    val F.it get() = this

    /**
     * Name the fixture to improve communication.
     */
    val F.fixture get() = this

    /**
     * Name the parentFixture improve communication.
     */
    val ParentF.parentFixture get() = this

    /**
     * Add a transform to be applied to the tests in this context and its children.
     */
    abstract fun addTransform(transform: TestTransform<F>)

    /**
     * An experimental map of properties that will be made available later in the test run.
     */
    val properties: MutableMap<Any, Any> = HashMap()

    /**
     * Internal implementation, only public to be accessible to inline functions.
     */
    abstract fun <G> internalCreateContext(
        name: String,
        type: FixtureType,
        fixtureFactory: (F.(TestDescriptor) -> G)?,
        builder: Context<F, G>.() -> Unit
    ): NodeBuilder<F>

    /**
     * Define the fixture that will be used in this context's tests and sub-contexts.
     *
     * Has access to the parent context's fixture as 'this'
     */
    @Suppress("FunctionName")
    @Deprecated("Replace with deriveFixture", ReplaceWith("deriveFixture"))
    fun fixture_(factory: ParentF.() -> F) = deriveFixture(factory)

    /**
     * Define the fixture that will be used in this context's tests and sub-contexts by
     * transforming the parent fixture.
     */
    @Deprecated("Replace with deriveFixture")
    fun mapFixture(f: (parentFixture: ParentF) -> F) = deriveFixture { f(this) }
}