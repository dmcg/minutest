package dev.minutest

/**
 * A function that can be invoked on a fixture, with a [TestDescriptor] describing the context.
 */
typealias Testlet<F> = (F, TestDescriptor) -> F

interface Testlet2<out F2, in F: F2> {
  fun thing(f: F, t: TestDescriptor) : F2
}
