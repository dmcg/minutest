package dev.minutest

import dev.minutest.Instrumented.test
import dev.minutest.Instrumented.test_
import dev.minutest.internal.FixtureType
import dev.minutest.internal.askType

/**
 * [ContextBuilder]s allow definition of tests and sub-contexts, all of which have the fixture type F.
 */
typealias ContextBuilder<F> = TestContextBuilder<*, F>

/**
 * A [ContextBuilder] where the type of the parent fixture [PF] is also accessible.
 */
@Suppress("DeprecatedCallableAddReplaceWith")
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
     * @see [context_]
     */
    @Deprecated(
        "use context_ { parentFixture -> .. } to specify a derived context",
        ReplaceWith("context_")
    )
    inline fun <reified G> derivedContext(
        name: String,
        noinline block: TestContextBuilder<F, G>.() -> Unit
    ): Annotatable<F> {
        return context_(name, block)
    }

    /**
     * Define a child-context with a different fixture type.
     *
     * You will have to call [given_] (with an underscore) in the sub-context to convert
     * from the parent to the child fixture type.
     */
    inline fun <reified G> context_(
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
    @Deprecated("use given to specify the fixture")
    fun fixture(factory: (Unit).(testDescriptor: TestDescriptor) -> F) {
        setFixtureFactory { testDescriptor ->
            Unit.factory(testDescriptor)
        }
    }

    /**
     * Define the fixture that will be used in this context's tests and sub-contexts by
     * transforming the parent fixture, accessible as the receiver 'this'.
     */
    @Deprecated("use given_ { parentFixture -> .. } to derive a fixture")
    fun deriveFixture(f: (PF).(testDescriptor: TestDescriptor) -> F) {
        setDerivedFixtureFactory(f)
    }

    /**
     * Apply an operation to the current fixture (accessible as the receiver 'this')
     * before running tests or sub-contexts.
     */
    @Deprecated("Use beforeEach to modify the fixture")
    fun modifyFixture(operation: F.(TestDescriptor) -> Unit) {
        addBefore { fixture, testDescriptor ->
            fixture.operation(testDescriptor)
            fixture
        }
    }

    /**
     * Define a test on the current fixture (accessible as 'this').
     */
    @Deprecated("use test")
    fun old_test(
        name: String,
        f: F.(testDescriptor: TestDescriptor) -> Unit
    ): Annotatable<F> {
        return test(name) { fixture, testDescriptor ->
            fixture.f(testDescriptor)
        }
    }

    /**
     * Define a test on the current fixture (accessible as the receiver 'this'), returning
     * a new fixture to be processed by 'afters'.
     */
    @Suppress("FunctionName")
    @Deprecated("use test_")
    fun old_test_(
        name: String,
        f: F.(testDescriptor: TestDescriptor) -> F
    ): Annotatable<F> {
        return test_(name) { fixture, testDescriptor ->
            fixture.f(testDescriptor)
        }
    }

    /**
     * Apply an operation to the current fixture (accessible as the receiver 'this') before
     * running tests or sub-contexts.
     */
    @Deprecated("Use beforeEach")
    fun before(operation: F.(TestDescriptor) -> Unit) {
        addBefore { fixture, testDescriptor ->
            fixture.operation(testDescriptor)
            fixture
        }
    }

    /**
     * Replace the current fixture (accessible as the receiver 'this') before
     * running tests or sub-contexts.
     */
    @Suppress("FunctionName")
    @Deprecated("Use beforeEach_")
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
    @Deprecated("Use afterEach")
    fun after(operation: F.(TestDescriptor) -> Unit) {
        addAfter { result, testDescriptor ->
            result.value.operation(testDescriptor)
        }
    }

    /**
     * Name the fixture to improve communication.
     */
    @Deprecated("Use the new DSL to get the fixture as a lambda parameter")
    val F.fixture: F
        get() = this

    /**
     * Name the parentFixture to improve communication.
     */
    @Deprecated("Use the new DSL to get the parent fixture as a lambda parameter")
    val PF.parentFixture: PF
        get() = this

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

    internal abstract fun setFixtureFactory(
        factory: (testDescriptor: TestDescriptor) -> F
    )

    internal abstract fun setDerivedFixtureFactory(
        factory: (parentFixture: PF, testDescriptor: TestDescriptor) -> F
    )

    internal abstract fun addBefore(transform: (F, TestDescriptor) -> F)
    internal abstract fun addAfter(
        operation: (FixtureValue<F>, TestDescriptor) -> Unit
    )

    internal abstract fun addBeforeAll(f: (TestDescriptor) -> Unit)
    internal abstract fun addAfterAll(f: (TestDescriptor) -> Unit)
}