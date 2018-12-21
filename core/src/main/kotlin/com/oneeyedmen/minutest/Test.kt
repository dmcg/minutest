package com.oneeyedmen.minutest

/**
 * A test that can be invoked on a fixture.
 */
interface Test<F> : (F, TestDescriptor) -> F {

    companion object {
        operator fun <F> invoke(block: (F, TestDescriptor) -> F) = object : Test<F> {
            override fun invoke(fixture: F, testDescriptor: TestDescriptor) = block(fixture, testDescriptor)
        }
    }
}

typealias TestTransform<F> = (Test<F>) -> Test<F>
