package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.context
import org.junit.jupiter.api.Test


class TransformTests {
    @Test
    fun `transforms wrap around application of before and after blocks`() {
        val log = mutableListOf<String>()
        
        executeTests(context<Unit> {
            before { log.add("before") }
            after { log.add("after") }
            
            addTransform { test ->
                test.withAction { fixture ->
                    log.add("entering transformed test")
                    test(fixture)
                    log.add("leaving transformed test")
                }
            }
            
            test("the test") { log.add("the test") }
        })
        
        assertLogged(log,
            "entering transformed test",
            "before",
            "the test",
            "after",
            "leaving transformed test"
        )
    }

    @Test
    fun `transforms nest, following nesting of contexts`() {
        val log = mutableListOf<String>()

        executeTests(context<Unit> {
            addTransform { test ->
                test.withAction { fixture ->
                    log.add("entering outer transformed test")
                    test(fixture)
                    log.add("leaving outer transformed test")
                }
            }

            before { log.add("before outer") }
            after { log.add("after outer") }

            context("inner") {
                addTransform { test ->
                    test.withAction { fixture ->
                        log.add("entering inner transformed test")
                        test(fixture)
                        log.add("leaving inner transformed test")
                    }
                }

                before { log.add("before inner") }
                after { log.add("after inner") }

                test("the test") { log.add("the test") }
            }
        })
        
        assertLogged(log,
            "entering outer transformed test",
            "before outer",
            "entering inner transformed test",
            "before inner",
            "the test",
            "after inner",
            "leaving inner transformed test",
            "after outer",
            "leaving outer transformed test"
        )
    }
    
    @Test
    fun `transforms can disable tests`() {
        val log = mutableListOf<String>()
        
        executeTests(context<Unit> {
            addTransform { test ->
                test.withAction { /* no op */ }
            }
            
            test("the test") { log.add("the test was invoked, but should not have been") }
        })
        
        assertNothingLogged(log)
    }
}