package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.NodeBuilder

interface JUnit4Minutests : JUnitXMinutests{
    override val tests: NodeBuilder<Unit> // a clue to what to override
}