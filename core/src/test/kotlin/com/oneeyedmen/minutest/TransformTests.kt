package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.TestFactory


object TransformTests {

    @TestFactory fun `test transform`() = junitTests<Unit> {
        addTransform { test ->
            object : Test<Unit> {
                override val name: String = test.name
                override fun invoke(fixture: Unit) {}
            }
        }

        test("transform can ignore test") {
            fail("Shouldn't get here")
        }
    }
}