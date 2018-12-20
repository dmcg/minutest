package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.experimental.deriveFixtureInstrumented
import org.junit.jupiter.api.Assertions.assertEquals


class NamingTests {
    
    @org.junit.jupiter.api.Test
    fun `fully qualified name`() {
        val log = mutableListOf<List<String>>()
        
        executeTests(rootContext<Unit> {
            addTransform { test ->
                test.withAction { fixture, descriptor ->
                    log.add(descriptor.fullName())
                    test(fixture, descriptor)
                }
            }

            context("outer") {
                test("outer test") {}

                context("inner") {
                    test("inner test 1") {}
                    test("inner test 2") {}
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
    
    @org.junit.jupiter.api.Test
    fun `names are passed to deriveFixtureInstrumented`() {
        val log = mutableListOf<List<String>>()

        class Fixture(val name: List<String>)

        executeTests(rootContext<Fixture> {

            deriveFixtureInstrumented { testDescriptor ->
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