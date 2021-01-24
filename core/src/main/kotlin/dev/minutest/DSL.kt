package dev.minutest

/**
 * Apply an operation to the current fixture before running tests or sub-contexts.
 */
fun <PF, F> TestContextBuilder<PF, F>.beforeEachInstrumented(
    operation: F.(fixture: F, testDescriptor: TestDescriptor) -> Unit
) {
    beforeEachInstrumented_ { fixture, testDescriptor ->
        fixture.operation(fixture, testDescriptor)
        this
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
    transform: F.(fixture: F, testDescriptor: TestDescriptor) -> F
) {
    addBefore { fixture, testDescriptor ->
        fixture.transform(fixture, testDescriptor)
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
    f: (F).(fixture: F, testDescriptor: TestDescriptor) -> Unit
): Annotatable<F> {
    return addTest(name) { fixture, testDescriptor ->
        fixture.f(fixture, testDescriptor)
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