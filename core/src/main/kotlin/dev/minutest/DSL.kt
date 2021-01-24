package dev.minutest

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
    f: (F).(fixture: F, testDescriptor: TestDescriptor) -> F
): Annotatable<F> {
    return addTest(name) { fixture, testDescriptor ->
        fixture.f(fixture, testDescriptor)
    }
}