package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.TestContext
import com.oneeyedmen.minutest.internal.MiContext
import com.oneeyedmen.minutest.internal.topContext
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.TestFactory
import java.util.stream.Stream

interface JupiterTests {

    val tests: TestContext<*, *>

    /**
     * Provided so that JUnit will run the tests
     */
    @TestFactory
    fun tests(): Stream<out DynamicNode> = (tests as MiContext<*, *>).toDynamicNodes()

    /**
     * Define a group of tests.
     */
    fun <F> context(builder: TestContext<Unit, F>.() -> Unit): TestContext<Unit, F> =
        topContext(this.javaClass.canonicalName, builder = builder)

    /**
     * Define a group of tests.
     */
    fun fixturelessContext(builder: TestContext<Unit, Unit>.() -> Unit): TestContext<Unit, Unit> =
        topContext(this.javaClass.canonicalName, fixtureFn = { Unit }, builder = builder)
}