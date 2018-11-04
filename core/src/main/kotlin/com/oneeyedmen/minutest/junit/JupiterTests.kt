package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.internal.ContextBuilder
import com.oneeyedmen.minutest.internal.asKType
import com.oneeyedmen.minutest.internal.topLevelContext
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.TestFactory
import java.util.stream.Stream

interface JupiterTests {

    val tests: Context<Unit, *>

    /**
     * Provided so that JUnit will run the tests
     */
    @TestFactory
    fun tests(): Stream<out DynamicNode> = (tests as ContextBuilder<Unit, *>).toStreamOfDynamicNodes()
}

/**
 * Define a group of tests.
 */
inline fun <reified F> JupiterTests.context(noinline builder: Context<Unit, F>.() -> Unit): Context<Unit, F> =
    topLevelContext(javaClass.canonicalName, asKType<F>(), builder = builder)

inline fun <reified F> JupiterTests.context(fixture: F, noinline builder: Context<Unit, F>.() -> Unit): Context<Unit, F> =
    topLevelContext(javaClass.canonicalName, asKType<F>(), fixture, builder = builder)

