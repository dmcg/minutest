package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.experimental.deriveFixtureInstrumented
import com.oneeyedmen.minutest.junit.context
import org.junit.jupiter.api.Assertions.assertEquals


class NamingTests {
    
    @org.junit.jupiter.api.Test
    fun `fully qualified name`() {
        val log = mutableListOf<List<String>>()
        
        executeTests(context<Unit> {
            addTransform {
                it.also { log.add(it.fullName()) }
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
                listOf(javaClass.canonicalName, "outer", "outer test"),
                listOf(javaClass.canonicalName, "outer", "inner", "inner test 1"),
                listOf(javaClass.canonicalName, "outer", "inner", "inner test 2")
            ),
            log
        )
    }
    
    @org.junit.jupiter.api.Test
    fun `names are passed to deriveFixtureInstrumented`() {
        val log = mutableListOf<List<String>>()

        class Fixture(val name: List<String>)

        executeTests(context<Fixture> {

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
                listOf(javaClass.canonicalName, "outer", "outer test"),
                listOf(javaClass.canonicalName, "outer", "inner", "inner test 1"),
                listOf(javaClass.canonicalName, "outer", "inner", "inner test 2")
            ),
            log
        )
    }
}