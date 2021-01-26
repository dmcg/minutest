package dev.minutest

/**
 * A version of the DSL that gives access to a [TestDescriptor]
 * describing the test being run.
 */
object Instrumented {

    fun <PF, F> TestContextBuilder<PF, F>.beforeEach(
        operation: F.(fixture: F, testDescriptor: TestDescriptor) -> Unit
    ) {
        beforeEach_ { fixture, testDescriptor ->
            operation(fixture, fixture, testDescriptor)
            fixture
        }
    }

    @Suppress("FunctionName")
    fun <PF, F> TestContextBuilder<PF, F>.beforeEach_(
        transform: F.(fixture: F, testDescriptor: TestDescriptor) -> F
    ) {
        addBefore { fixture, testDescriptor ->
            transform(fixture, fixture, testDescriptor)
        }
    }

    fun <PF, F> TestContextBuilder<PF, F>.given(
        factory: (testDescriptor: TestDescriptor) -> F
    ) {
        setFixtureFactory { testDescriptor ->
            factory(testDescriptor)
        }
    }

    @Suppress("FunctionName")
    fun <PF, F> TestContextBuilder<PF, F>.given_(
        transform: (parentFixture: PF, testDescriptor: TestDescriptor) -> F
    ) {
        setDerivedFixtureFactory { parentFixture, testDescriptor ->
            transform(parentFixture, testDescriptor)
        }
    }

    fun <PF, F> TestContextBuilder<PF, F>.test(
        name: String,
        f: F.(fixture: F, testDescriptor: TestDescriptor) -> Unit
    ): Annotatable<F> {
        return addTest(name) { fixture, testDescriptor ->
            f(fixture, fixture, testDescriptor)
            fixture
        }
    }

    @Suppress("FunctionName")
    fun <PF, F> TestContextBuilder<PF, F>.test_(
        name: String,
        f: F.(fixture: F, testDescriptor: TestDescriptor) -> F
    ): Annotatable<F> {
        return addTest(name) { fixture, testDescriptor ->
            f(fixture, fixture, testDescriptor)
        }
    }

    fun <PF, F> TestContextBuilder<PF, F>.afterEach(
        operation: (fixtureValue: FixtureValue<F>, testDescriptor: TestDescriptor) -> Unit
    ) {
        addAfter { fixtureValue, testDescriptor ->
            operation(fixtureValue, testDescriptor)
        }
    }
}