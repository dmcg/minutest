package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertEquals


object NamingTests {
    
    @org.junit.jupiter.api.Test
    fun `fully qualified name`() {
        val log = mutableListOf<List<String>>()
        
        executeTest(junitTests<Unit> {
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
}