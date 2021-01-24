package dev.minutest

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
fun <PF, F> TestContextBuilder<PF, F>.instrumentedBeforeEach_(
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

fun <PF, F> TestContextBuilder<PF, F>.instrumentedTest2(
    name: String,
    f: (F).(fixture: F, testDescriptor: TestDescriptor) -> Unit
): Annotatable<F> {
    return addTest(name) { fixture, testDescriptor ->
        fixture.f(fixture, testDescriptor)
        fixture
    }
}

fun <PF, F> TestContextBuilder<PF, F>.test2_(
    name: String,
    f: (F).(fixture: F) -> F
): Annotatable<F> {
    return addTest(name) { fixture, _ ->
        fixture.f(fixture)
    }
}

fun <PF, F> TestContextBuilder<PF, F>.instrumentedTest2_(
    name: String,
    f: (fixture: F, testDescriptor: TestDescriptor) -> F
): Annotatable<F> {
    return addTest(name) { fixture, testDescriptor ->
        f(fixture, testDescriptor)
    }
}