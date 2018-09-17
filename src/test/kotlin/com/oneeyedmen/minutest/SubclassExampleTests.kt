package com.oneeyedmen.minutest

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.TestFactory
import java.util.*



object SubclassExampleTests {

    @TestFactory fun `generate contexts to test with multiple values`() = context<MutableCollection<String>> {

        val factories = listOf<() -> MutableCollection<String>>(::ArrayList, ::LinkedList)

        factories.forEach { factory ->
            context("check $factory") {

                fixture { factory() }

                test("is empty") {
                    assertTrue(isEmpty())
                }

                test("can add") {
                    add("item")
                    assertEquals("item", first())
                }
            }
        }
    }
}