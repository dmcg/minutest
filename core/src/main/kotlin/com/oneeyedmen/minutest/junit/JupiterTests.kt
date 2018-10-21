package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.TestContext
import com.oneeyedmen.minutest.internal.MiContext
import com.oneeyedmen.minutest.testContext
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.TestFactory
import java.util.stream.Stream

interface JupiterTests {

    val tests: TestContext<*>

    /**
     * Provided so that JUnit will run the tests
     */
    @TestFactory
    fun tests(): Stream<out DynamicNode> = (tests as MiContext<*, *>).toDynamicNodes()
}

/**
 * Define a group of tests.
 */
@Suppress("unused") // keep receiver to scope this to JupiterTests
inline fun <reified F> JupiterTests.context(noinline builder: TestContext<F>.() -> Unit): TestContext<F> =
    testContext(rootContextName, builder)
