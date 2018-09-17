package com.oneeyedmen.minutest

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.TestFactory
import java.util.*



object SubclassExampleTests {

    @TestFactory fun `pass factories into a context builder`() = context<MutableCollection<String>> {

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

    // or define a reusable spec in one place
    fun TestContext<MutableCollection<String>>.collectionTests(factory: () -> MutableCollection<String>) {
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

    // and use it in others
    @TestFactory fun `use predefined spec for ArrayList`() = context<MutableCollection<String>> {
        collectionTests { ArrayList() }
    }

    @TestFactory fun `use predefined spec for LinkedList`() = context<MutableCollection<String>> {
        collectionTests { LinkedList() }
    }
}