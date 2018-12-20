package com.oneeyedmen.minutest

/**
 * A test that can be invoked on a fixture.  A test has a name and is defined within a nested tree of named things.
 */
interface Test<F> : Named, (F, TestDescriptor) -> F

fun <F> Test<F>.withAction(action: (F, TestDescriptor) -> F): Test<F> =
    object : Test<F>, (F, TestDescriptor) -> F by action, Named by this {}

typealias TestTransform<F> = (Test<F>) -> Test<F>
