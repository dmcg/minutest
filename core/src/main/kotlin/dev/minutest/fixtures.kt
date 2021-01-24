package dev.minutest

/**
 * Define a fixture that will automatically be closed when the context is done with.
 */
fun <F: AutoCloseable> ContextBuilder<F>.closeableFixture(
    factory: (Unit).(testDescriptor: TestDescriptor) -> F
) = fixture { testDescriptor ->
    factory(testDescriptor)
}.also {
    afterEach {
        it.close()
    }
}