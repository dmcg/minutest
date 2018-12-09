package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.NodeBuilder
import com.oneeyedmen.minutest.buildRootNode
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.TestFactory
import java.util.stream.Stream

@Deprecated("JupiterTests is now JUnit5Minutests", replaceWith = ReplaceWith("JUnit5Minutests"))
typealias JupiterTests = JUnit5Minutests

interface JUnit5Minutests : JUnitXMinutests {

    override val tests: NodeBuilder<Unit> // a clue to what to override

    /**
     * Provided so that JUnit will run the tests
     */
    @TestFactory
    fun tests(): Stream<out DynamicNode> = tests.buildRootNode().toStreamOfDynamicNodes()
}