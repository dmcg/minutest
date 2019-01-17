package com.oneeyedmen.minutest

/**
 * A test that can be invoked on a fixture, with a [TestDescriptor] describing the context.
 */
typealias Test<F> = (F, TestDescriptor) -> F
