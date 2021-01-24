package dev.minutest

import dev.minutest.Instrumented.given

/**
 * Define a fixture that will automatically be closed when the context is done with.
 */
fun <F: AutoCloseable> ContextBuilder<F>.givenClosable(
    factory: (testDescriptor: TestDescriptor) -> F
) = given { testDescriptor ->
    factory(testDescriptor)
}.also {
    afterEach {
        it.close()
    }
}