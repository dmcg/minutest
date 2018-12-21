package com.oneeyedmen.minutest

/**
 * A test that can be invoked on a fixture.
 */
interface Test<F> : (F, TestDescriptor) -> F {

    val name: String
    // We only need this name because we don't pass it in the TestDescriptor

    companion object {
        operator fun <F> invoke(name: String, block: (F, TestDescriptor) -> F) = object : Test<F> {
            override val name get() = name
            override fun invoke(fixture: F, testDescriptor: TestDescriptor) = block(fixture, testDescriptor)
        }
    }
}

fun <F> Test<F>.withAction(action: (F, TestDescriptor) -> F): Test<F> = Test(name, action)

typealias TestTransform<F> = (Test<F>) -> Test<F>
