package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertEquals


object TransformTests {
    
    @org.junit.jupiter.api.Test
    fun `transforms wrap around application of before and after blocks`() {
        val log = mutableListOf<String>()
        
        executeTest(junitTests<Unit> {
            before {
                log.add("before")
            }
            
            addTransform { test ->
                object : Test<Unit>, Named by test {
                    override fun invoke(fixture: Unit) {
                        log.add("entering transformed test")
                        test(fixture)
                        log.add("leaving transformed test")
                    }
                }
            }
            
            after {
                log.add("after")
            }
            
            test("the test") {
                log.add("the test")
            }
        })
        
        assertEquals(
            listOf(
                "entering transformed test",
                "before",
                "the test",
                "after",
                "leaving transformed test"
            ),
            log
        )
    }
}