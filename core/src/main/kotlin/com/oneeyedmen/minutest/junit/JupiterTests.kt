package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.TestContext
import com.oneeyedmen.minutest.internal.MiContext
import com.oneeyedmen.minutest.internal.asKType
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.TestFactory
import java.util.stream.Stream


abstract class JupiterTests<F>(
    private val fixtureIsNullable: Boolean = false
) : IKnowMyGenericClass<F> {

    protected abstract val tests: TestContext<F>

    /**
     * Define a group of tests.
     */
    fun context(builder: TestContext<F>.() -> Unit): TestContext<F> =
        MiContext<Unit, F>(
            rootContextName,
            null,
            myGenericClass().asKType(fixtureIsNullable)).apply { builder() }

    /**
     * Synonym for [this::context]
     */
    fun tests(builder: TestContext<F>.() -> Unit): TestContext<F> = context(builder)

    /**
     * Provided so that JUnit will run the tests
     */
    @TestFactory
    fun tests(): Stream<out DynamicNode> = (tests as MiContext<*, *>).toDynamicNodes()
}