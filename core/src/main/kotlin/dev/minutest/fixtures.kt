package dev.minutest

/**
 * Inject a dependency into a fixture and close it later.
 */
fun <PF, F, D : Any> TestContextBuilder<PF, F>.lifecycleFixture(
    dependencyBuilder: () -> D,
    dependencyCloser: (D) -> Unit,
    factory: (Unit).(D) -> F
) = lifecycleFixture(
    dependencyBuilder = { dependencyBuilder() },
    dependencyCloser = { dependency, _ -> dependencyCloser(dependency) },
    factory = { dependency, _ -> factory(dependency) }
)

/**
 * Inject a dependency into a fixture and close it later (pedantic version).
 */
fun <PF, F, D : Any> TestContextBuilder<PF, F>.lifecycleFixture(
    dependencyBuilder: (TestDescriptor) -> D,
    dependencyCloser: (D, TestDescriptor) -> Unit,
    factory: (Unit).(D, TestDescriptor) -> F
) {
    lateinit var dependency: D // lateinit as we want to build dependency as late as possible but still see it in the after
    fixture { testDescriptor ->
        dependency = dependencyBuilder(testDescriptor)
        factory(dependency, testDescriptor)
    }
    after { testDescriptor ->
        dependencyCloser(dependency, testDescriptor)
    }
}