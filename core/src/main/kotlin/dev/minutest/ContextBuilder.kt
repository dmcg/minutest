package dev.minutest

import dev.minutest.internal.FixtureType
import dev.minutest.internal.askType

/**
 * [ContextBuilder]s allow definition of tests and sub-contexts, all of which have the fixture type [F].
 */
typealias ContextBuilder<F> = TestContextBuilder<*, F>

/**
 * A [ContextBuilder] where the type of the parent fixture [PF] is also accessible.
 */
@MinutestFixture // It isn't of course - but see explanation there.
abstract class TestContextBuilder<PF, F> {

    /**
     * Define a child-context, inheriting the fixture from the parent.
     */
    abstract fun context(name: String, block: TestContextBuilder<F, F>.() -> Unit): NodeBuilder<F>

    /**
     * Define a child-context with a different fixture type.
     *
     * You will have to call [deriveFixture] in the sub-context to convert from the parent
     * to the child fixture type.
     */
    inline fun <reified G> derivedContext(name: String, noinline block: TestContextBuilder<F, G>.() -> Unit)
        : NodeBuilder<F> = internalDerivedContext(name = name, newFixtureType = askType<G>(), block = block)

    /**
     * Define the fixture that will be used in this context's tests and sub-contexts.
     *
     * The strange parameter type keeps compatibility with the other fixture methods, that have
     * the parent fixture as the receiver.
     */
    abstract fun fixture(factory: (Unit).(testDescriptor: TestDescriptor) -> F)

    /**
     * Apply an operation to the current fixture (accessible as the receiver 'this')
     * before running tests or sub-contexts.
     */
    fun modifyFixture(operation: F.(TestDescriptor) -> Unit): Unit = before(operation)

    /**
     * Define the fixture that will be used in this context's tests and sub-contexts by
     * transforming the parent fixture, accessible as the receiver 'this'.
     */
    abstract fun deriveFixture(f: (PF).(testDescriptor: TestDescriptor) -> F)

    /**
     * Define a test on the current fixture (accessible as 'this').
     */
    fun test(name: String, f: F.(testDescriptor: TestDescriptor) -> Unit): NodeBuilder<F> =
        test_(name) { testDescriptor ->
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
    abstract fun before(operation: F.(TestDescriptor) -> Unit)

    /**
     * Replace the current fixture (accessible as the receiver 'this') before
     * running tests or sub-contexts.
     */
    abstract fun before_(f: F.(TestDescriptor) -> F)

    /**
     * Apply an operation to the current fixture (accessible as 'this') after running tests.
     *
     * Will be invoked even if tests or 'befores' throw exceptions.
     *
     * An exception thrown in an after will prevent later afters running.
     */
    abstract fun after(operation: F.(TestDescriptor) -> Unit)

    abstract fun after2(operation: FixtureValue<F>.(TestDescriptor) -> Unit)

    /**
     * Name the fixture to improve communication.
     */
    val F.fixture: F get() = this

    /**
     * Name the parentFixture to improve communication.
     */
    val PF.parentFixture: PF get() = this

    /**
     * Apply an operation after all the tests and sub-contexts have completed.
     */
    abstract fun afterAll(f: () -> Unit)

    /**
     * Internal implementation, only public to be accessible to inline functions.
     */
    @PublishedApi internal abstract fun <G> internalDerivedContext(
        name: String,
        newFixtureType: FixtureType,
        block: TestContextBuilder<F, G>.() -> Unit
    ): NodeBuilder<F>

}