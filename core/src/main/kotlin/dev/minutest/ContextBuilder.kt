package dev.minutest

import dev.minutest.internal.FixtureType
import dev.minutest.internal.askType

/**
 * [ContextBuilder]s allow definition of tests and sub-contexts, all of which have the fixture type F.
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
    fun context(
        name: String,
        block: TestContextBuilder<F, F>.() -> Unit
    ): Annotatable<F> {
        return addContext(name, block)
    }

    /**
     * Define a child-context with a different fixture type.
     *
     * You will have to call [deriveFixture] in the sub-context to convert from the parent
     * to the child fixture type.
     */
    inline fun <reified G> derivedContext(
        name: String,
        noinline block: TestContextBuilder<F, G>.() -> Unit
    ): Annotatable<F> {
        return addDerivedContext(name, askType<G>(), block)
    }

    /**
     * Define the fixture that will be used in this context's tests and sub-contexts.
     *
     * The strange parameter type keeps compatibility with the other fixture methods, that have
     * the parent fixture as the receiver.
     */
    fun fixture(factory: (Unit).(testDescriptor: TestDescriptor) -> F) {
        setFixtureFactory { testDescriptor ->
            Unit.factory(testDescriptor)
        }
    }

    /**
     * Apply an operation to the current fixture (accessible as the receiver 'this')
     * before running tests or sub-contexts.
     */
    fun modifyFixture(operation: F.(TestDescriptor) -> Unit): Unit = before(operation)

    /**
     * Define the fixture that will be used in this context's tests and sub-contexts by
     * transforming the parent fixture, accessible as the receiver 'this'.
     */
    fun deriveFixture(f: (PF).(testDescriptor: TestDescriptor) -> F) {
        setDerivedFixtureFactory(f)
    }

    /**
     * Define a test on the current fixture (accessible as 'this').
     */
    fun test(
        name: String,
        f: F.(testDescriptor: TestDescriptor) -> Unit
    ): Annotatable<F> =
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
    fun test_(
        name: String,
        f: F.(testDescriptor: TestDescriptor) -> F
    ): Annotatable<F> {
        return addTest(name, f)
    }

    /**
     * Apply an operation to the current fixture (accessible as the receiver 'this') before
     * running tests or sub-contexts.
     */
    fun before(operation: F.(TestDescriptor) -> Unit) {
        before_ { testDescriptor ->
            this.operation(testDescriptor)
            this
        }
    }

    /**
     * Replace the current fixture (accessible as the receiver 'this') before
     * running tests or sub-contexts.
     */
    fun before_(transform: F.(TestDescriptor) -> F) {
        addBefore(transform)
    }

    /**
     * Apply an operation to the last known value of the current fixture
     * (accessible as 'this') after running tests.
     *
     * Will be invoked even if tests or 'befores' throw exceptions.
     *
     * An exception thrown in an after will prevent later afters running.
     */

    fun after(operation: F.(TestDescriptor) -> Unit) {
        addAfter { result, testDescriptor ->
            result.value.operation(testDescriptor)
        }
    }

    /**
     * Version of [after] that gives access to the last known value of the fixture and
     * any exception thrown by previous operations as a [FixtureValue].
     */
    fun after2(operation: FixtureValue<F>.(TestDescriptor) -> Unit) {
        addAfter(operation)
    }

    /**
     * Name the fixture to improve communication.
     */
    val F.fixture: F get() = this

    /**
     * Name the parentFixture to improve communication.
     */
    val PF.parentFixture: PF get() = this

    /**
     * Apply an operation before any test in this or sub-contexts.
     */
    fun beforeAll(f: (TestDescriptor) -> Unit) {
        addBeforeAll(f)
    }

    /**
     * Apply an operation after all the tests and sub-contexts have completed.
     */
    fun afterAll(f: (TestDescriptor) -> Unit) {
        addAfterAll(f)
    }

    /**
     * Internal implementation, only public to be accessible to inline functions.
     */
    @PublishedApi
    internal abstract fun <G> addDerivedContext(
        name: String,
        newFixtureType: FixtureType,
        block: TestContextBuilder<F, G>.() -> Unit
    ): Annotatable<F>

    internal abstract fun addContext(
        name: String,
        block: TestContextBuilder<F, F>.() -> Unit
    ): Annotatable<F>

    internal abstract fun addTest(
        name: String,
        f: (F, TestDescriptor) -> F
    ): Annotatable<F>

    internal abstract fun setFixtureFactory(factory: (testDescriptor: TestDescriptor) -> F)
    internal abstract fun setDerivedFixtureFactory(f: PF.(TestDescriptor) -> F)

    internal abstract fun addBefore(transform: (F, TestDescriptor) -> F)
    internal abstract fun addAfter(
        operation: (FixtureValue<F>, TestDescriptor) -> Unit
    )

    internal abstract fun addBeforeAll(f: (TestDescriptor) -> Unit)
    internal abstract fun addAfterAll(f: (TestDescriptor) -> Unit)
}