package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.TestContext
import com.oneeyedmen.minutest.internal.asKType
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.TestFactory
import java.util.stream.Stream

/**
 * EXPERIMENTAL Base class for tests that you want run with JUnit 5
 */
abstract class JUnitTests<F>(
    override val tests: TestContext<F>.() -> Unit,
    override val fixtureIsNullable: Boolean = false
) : IRunTestsInJUnit5<F>

abstract class JUnitFixtureTests<F>(
    override val fixtureIsNullable: Boolean = false
) : IRunTestsInJUnit5<F> {

    // little thunk to prevent having to specify the type of the tests val
    protected fun tests(context: TestContext<F>.() -> Unit) = context
}

internal interface IRunTestsInJUnit5<F> : IKnowMyGenericClass<F> {
    val tests: TestContext<F>.() -> Unit
    val fixtureIsNullable: Boolean


    @TestFactory
    fun tests(): Stream<out DynamicNode> = junitTests(myGenericClass().asKType(fixtureIsNullable), tests)

}

