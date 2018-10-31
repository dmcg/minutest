package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.assertThrows
import kotlin.streams.asSequence


object TransformTests {

    @TestFactory fun `test transform`() = junitTests<Unit> {
        addTransform { test ->
            object : com.oneeyedmen.minutest.Test<Unit> {
                override val name: String = test.name
                override fun invoke(fixture: Unit) {}
            }
        }

        test("transform can ignore test") {
            fail("Shouldn't get here")
        }
    }
}