package com.oneeyedmen.minutest

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test as JupiterTest

class NamingTests {

    @JupiterTest
    fun `names are passed to deriveFixture`() {
        val log = mutableListOf<List<String>>()

        class Fixture(val name: List<String>)

        executeTests(rootContext<Fixture> {

            deriveFixture { testDescriptor ->
                Fixture(testDescriptor.fullName())
            }

            context("outer") {
                test("outer test") { log.add(name) }

                context("inner") {
                    test("inner test 1") { log.add(name) }
                    test("inner test 2") { log.add(name) }
                }
            }
        })

        assertEquals(
            listOf(
                listOf("root", "outer", "outer test"),
                listOf("root", "outer", "inner", "inner test 1"),
                listOf("root", "outer", "inner", "inner test 2")
            ),
            log
        )
    }
}