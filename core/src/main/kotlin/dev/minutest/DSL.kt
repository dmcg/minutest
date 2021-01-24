package dev.minutest

/**
 * Apply an operation to the current fixture before running tests or sub-contexts.
 */
fun <PF, F> TestContextBuilder<PF, F>.beforeEachInstrumented(
    operation: (fixture: F, testDescriptor: TestDescriptor) -> Unit
) {
    beforeEachInstrumented_ { fixture, testDescriptor ->
        operation(fixture, testDescriptor)
        fixture
    }
}

/**
 * Apply an operation to the current fixture before running tests or sub-contexts.
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
 * Replace the current fixture before running tests or sub-contexts.
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
 * Replace the current fixture before running tests or sub-contexts.
 */
@Suppress("FunctionName")
fun <PF, F> TestContextBuilder<PF, F>.beforeEachInstrumented_(
    transform: (fixture: F, testDescriptor: TestDescriptor) -> F
) {
    addBefore { fixture, testDescriptor ->
        transform(fixture, testDescriptor)
    }
}

fun <PF, F> TestContextBuilder<PF, F>.test2(
    name: String,
    f: (F).(fixture: F) -> Unit
): Annotatable<F> {
    return addTest(name) { fixture, _ ->
        fixture.f(fixture)
        fixture
    }
}

fun <PF, F> TestContextBuilder<PF, F>.test2Instrumented(
    name: String,
    f: (fixture: F, testDescriptor: TestDescriptor) -> Unit
): Annotatable<F> {
    return addTest(name) { fixture, testDescriptor ->
        f(fixture, testDescriptor)
        fixture
    }
}

@Suppress("FunctionName")
fun <PF, F> TestContextBuilder<PF, F>.test2_(
    name: String,
    f: (F).(fixture: F) -> F
): Annotatable<F> {
    return addTest(name) { fixture, _ ->
        fixture.f(fixture)
    }
}

fun <PF, F> TestContextBuilder<PF, F>.test2Instrumented_(
    name: String,
    f: (fixture: F, testDescriptor: TestDescriptor) -> F
): Annotatable<F> {
    return addTest(name) { fixture, testDescriptor ->
        f(fixture, testDescriptor)
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

/**
 * Apply an operation to the last known value of the current fixture
 * after running tests.
 *
 * Will be invoked even if tests or 'befores' throw exceptions.
 *
 * An exception thrown in an afterEach will prevent later afters running.
 *
 * Gives access to the last known value of the fixture and
 * any exception thrown by previous operations as a [FixtureValue].

 */
fun <PF, F> TestContextBuilder<PF, F>.afterEachInstrumented(
    operation: (fixtureValue: FixtureValue<F>, testDescriptor: TestDescriptor) -> Unit
) {
    addAfter { fixtureValue, testDescriptor ->
        operation(fixtureValue, testDescriptor)
    }
}