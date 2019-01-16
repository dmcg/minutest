package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.experimental.TestAnnotation
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
    fun fixture(factory: (Unit).(testDescriptor: TestDescriptor) -> F) = deriveFixture { testDescriptor ->
        Unit.factory(testDescriptor)
    }

    /**
     * Apply an operation to the current fixture (accessible as the receiver 'this')
     * before running tests or sub-contexts.
     */
    fun modifyFixture(operation: F.() -> Unit) = before(operation)

    /**
     * Define the fixture that will be used in this context's tests and sub-contexts by
     * transforming the parent fixture, accessible as the receiver 'this'.
     */
    abstract fun deriveFixture(f: (ParentF).(testDescriptor: TestDescriptor) -> F)

    /**
     * Define a test on the current fixture (accessible as 'this').
     */
    fun test(name: String, f: F.(testDescriptor: TestDescriptor) -> Unit) = test_(name) { testDescriptor ->
        this.apply {
            f(testDescriptor)
        }
    }

    /**
     * Define a test on the current fixture (accessible as the receiver 'this'), returning
     * a new fixture to be processed by 'afters'.
     */
    @Suppress("FunctionName")
    abstract fun test_(name: String, f: F.(testDescriptor: TestDescriptor) -> F): NodeBuilder<F>

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
     * Name the fixture to improve communication.
     */
    val F.fixture get() = this

    /**
     * Name the parentFixture to improve communication.
     */
    val ParentF.parentFixture get() = this

    val annotations: MutableList<TestAnnotation> = mutableListOf()

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
     * Apply an operation after all the tests and sub-contexts have completed.
     */
    abstract fun afterAll(f: () -> Unit)
}