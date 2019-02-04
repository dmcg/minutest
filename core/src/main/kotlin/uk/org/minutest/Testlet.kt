package uk.org.minutest

/**
 * A function that can be invoked on a fixture, with a [TestDescriptor] describing the context.
 */
typealias Testlet<F> = (F, TestDescriptor) -> F
