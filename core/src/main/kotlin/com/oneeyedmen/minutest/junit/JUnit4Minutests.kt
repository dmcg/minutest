package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.NodeBuilder
import org.junit.runner.RunWith

@RunWith(MinutestJUnit4Runner::class)
abstract class JUnit4Minutests : JUnitXMinutests {
    abstract override val tests: NodeBuilder<Unit> // a clue to what to override
}