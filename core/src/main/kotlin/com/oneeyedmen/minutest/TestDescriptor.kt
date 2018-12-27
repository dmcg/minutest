package com.oneeyedmen.minutest

/**
 * The context of a test execution.
 */
interface TestDescriptor {
    val name: String
    val parent: TestDescriptor?

    fun fullName() =
        generateSequence(this, TestDescriptor::parent)
            .filter { it !is RootDescriptor }
            .map(TestDescriptor::name)
            .toList()
            .reversed()
}

interface RootDescriptor

