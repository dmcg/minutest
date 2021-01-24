package dev.minutest

/**
 * Define a fixture that will automatically be closed when the context is done with.
 */
fun <F: AutoCloseable> ContextBuilder<F>.givenClosable(
    factory: (testDescriptor: TestDescriptor) -> F
) = givenInstrumented { testDescriptor ->
    factory(testDescriptor)
}.also {
    afterEach {
        it.close()
    }
}