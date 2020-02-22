package dev.minutest

/**
 * Define a fixture that will automatically be closed when the context is done with.
 */
fun <F: AutoCloseable> ContextBuilder<F>.closeableFixture(
    factory: (Unit).(testDescriptor: TestDescriptor) -> F
) = fixture { testDescriptor ->
    factory(testDescriptor)
}.also {
    after {
        fixture.close()
    }
}

/**
 * Define a fixture that needs a disposable dependency.
 */
fun <F, D : Any> ContextBuilder<F>.lifecycleFixture(
    dependencyBuilder: () -> D,
    dependencyDisposer: (D) -> Unit,
    factory: (Unit).(D) -> F
) = lifecycleFixture(
    dependencyBuilder = { dependencyBuilder() },
    dependencyDisposer = { dependency, _ -> dependencyDisposer(dependency) },
    factory = { dependency, _ -> factory(dependency) }
)

/**
 * Define a fixture that needs a disposable dependency (pedantic version).
 */
fun <F, D : Any> ContextBuilder<F>.lifecycleFixture(
    dependencyBuilder: (TestDescriptor) -> D,
    dependencyDisposer: (D, TestDescriptor) -> Unit,
    factory: (Unit).(D, TestDescriptor) -> F
) {
    lateinit var dependency: D // lateinit as we want to build dependency as late as possible but still see it in the after
    fixture { testDescriptor ->
        dependency = dependencyBuilder(testDescriptor)
        factory(dependency, testDescriptor)
    }
    after { testDescriptor ->
        dependencyDisposer(dependency, testDescriptor)
    }
}