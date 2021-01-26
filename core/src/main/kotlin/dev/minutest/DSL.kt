package dev.minutest


/**
 * Apply an operation to the current fixture
 * (accessible as 'this' and 'it')
 * before running tests or sub-contexts.
 */
fun <PF, F> TestContextBuilder<PF, F>.beforeEach(
    operation: F.(fixture: F) -> Unit
) {
    beforeEach_ { fixture ->
        fixture.operation(fixture)
        this
    }
}

/**
 * Replace the current fixture
 * (accessible as 'this' and 'it')
 * before running tests or sub-contexts.
 */
@Suppress("FunctionName")
fun <PF, F> TestContextBuilder<PF, F>.beforeEach_(
    transform: F.(fixture: F) -> F
) {
    addBefore { fixture, _ ->
        fixture.transform(fixture)
    }
}

/**
 * Define the fixture that will be used in this context's tests and sub-contexts.
 */
fun <PF, F> TestContextBuilder<PF, F>.given(factory: () -> F) {
    setFixtureFactory {
        factory()
    }
}

/**
 * Define the fixture that will be used in this context's tests and sub-contexts as
 * a transform on the parent fixture.
 */
@Suppress("FunctionName")
fun <PF, F> TestContextBuilder<PF, F>.given_(
    transform: (parentFixture: PF) -> F
) {
    setDerivedFixtureFactory { parentFixture, _ ->
        transform(parentFixture)
    }
}

/**
 * Define a test on the current fixture
 * (accessible as 'this' and 'it').
 */
fun <PF, F> TestContextBuilder<PF, F>.test2(
    name: String,
    f: F.(fixture: F) -> Unit
): Annotatable<F> {
    return addTest(name) { fixture, _ ->
        fixture.f(fixture)
        fixture
    }
}
/**
 * Define a test on the current fixture
 * (accessible as 'this' and 'it'),
 * returning a new fixture to be processed by afterEach's.
 */
@Suppress("FunctionName")
fun <PF, F> TestContextBuilder<PF, F>.test2_(
    name: String,
    f: F.(fixture: F) -> F
): Annotatable<F> {
    return addTest(name) { fixture, _ ->
        fixture.f(fixture)
    }
}

/**
 * Apply an operation to the last known value of the current fixture
 * after running tests.
 *
 * Will be invoked even if tests or 'befores' throw exceptions.
 *
 * An exception thrown in an afterEach will prevent later afters running.
 */
fun <PF, F> TestContextBuilder<PF, F>.afterEach(operation: F.(fixture: F) -> Unit) {
    addAfter { (value, _), _->
        value.operation(value)
    }
}