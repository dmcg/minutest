package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.Context
import com.oneeyedmen.minutest.RuntimeNode
import com.oneeyedmen.minutest.buildRootNode
import com.oneeyedmen.minutest.internal.askType
import com.oneeyedmen.minutest.internal.topLevelContext
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.TestFactory
import java.util.stream.Stream

interface JupiterTests {

    val tests: RuntimeNode

    /**
     * Provided so that JUnit will run the tests
     */
    @TestFactory
    fun tests(): Stream<out DynamicNode> = tests.toStreamOfDynamicNodes()
}

/**
 * Define a group of tests.
 */
inline fun <reified F> JupiterTests.context(
    transform: (RuntimeNode) -> RuntimeNode = { it },
    noinline builder: Context<Unit, F>.() -> Unit
) =
    topLevelContext(javaClass.canonicalName, askType<F>(), builder = builder)
        .buildRootNode()
        .run(transform)

inline fun <reified F> JupiterTests.context(
    fixture: F,
    transform: (RuntimeNode) -> RuntimeNode = { it },
    noinline builder: Context<Unit, F>.() -> Unit
) =
    topLevelContext(javaClass.canonicalName, askType<F>(), fixture, builder = builder)
        .buildRootNode()
        .run(transform)

