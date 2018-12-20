package com.oneeyedmen.minutest

/**
 * A test that can be invoked on a fixture.
 */
interface Test<F> : (F, TestDescriptor) -> F {
    val name: String
}

fun <F> Test<F>.withAction(action: (F, TestDescriptor) -> F): Test<F> =
    object : Test<F>, (F, TestDescriptor) -> F by action {
        override val name = this@withAction.name
    }

typealias TestTransform<F> = (Test<F>) -> Test<F>
