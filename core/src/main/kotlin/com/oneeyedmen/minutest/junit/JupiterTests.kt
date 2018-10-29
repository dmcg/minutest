package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.internal.MiContext
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
    fun tests(): Stream<out DynamicNode> = (tests as MiContext<Unit, *>).toStreamOfDynamicNodes()
}

/**
 * Define a group of tests.
 */
inline fun <reified F> JupiterTests.context(noinline builder: Context<Unit, F>.() -> Unit): Context<Unit, F> =
    topLevelContext(javaClass.canonicalName, F::class.isInstance(Unit), builder = builder)

