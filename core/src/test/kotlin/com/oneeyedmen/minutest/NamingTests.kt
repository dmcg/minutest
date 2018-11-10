package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertEquals


object NamingTests {
    
    @org.junit.jupiter.api.Test
    fun `fully qualified name`() {
        val log = mutableListOf<List<String>>()
        
        executeTest(junitTests<Unit> {
            addTransform { test ->
                test.also { log.add(it.fullName()) }
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
    fun `names are available as testDescriptor property`() {
        val log = mutableListOf<List<String>>()

        class Fixture(val name: List<String>)

        executeTest(junitTests<Fixture> {

            fixture {
                Fixture(testDescriptor.fullName())
            }

            // testDescriptor not available here

            context("outer") {

                deriveFixture {
                    assertEquals(name, testDescriptor.fullName())
                    this
                }

                before {
                    assertEquals(name, testDescriptor.fullName())
                }

                test("outer test") {
                    assertEquals(name, testDescriptor.fullName())
                    log.add(name)
                }
                
                context("inner") {
                    test("inner test 1") {
                        assertEquals(name, testDescriptor.fullName())
                        log.add(name)
                    }
                    test("inner test 2") {
                        assertEquals(name, testDescriptor.fullName())
                        log.add(name)
                    }
                }

                after {
                    assertEquals(name, testDescriptor.fullName())
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