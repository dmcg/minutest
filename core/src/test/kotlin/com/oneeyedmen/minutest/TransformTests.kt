package com.oneeyedmen.minutest

import org.junit.jupiter.api.Test as JUnitTest


class TransformTests {
    @JUnitTest
    fun `transforms wrap around application of before and after blocks`() {
        val log = mutableListOf<String>()

        val transform: TestTransform<Unit> = { test ->
            { fixture: Unit, descriptor: TestDescriptor ->
                log.add("entering transformed test")
                test(fixture, descriptor)
                log.add("leaving transformed test")
            }
        }

        executeTests(rootContext<Unit> {
            before { log.add("before") }
            after { log.add("after") }
            addTransform(transform)

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

    @JUnitTest
    fun `transforms nest, following nesting of contexts`() {
        val log = mutableListOf<String>()

        val outerTransform: TestTransform<Unit> = { test ->
            { fixture: Unit, descriptor: TestDescriptor ->
                log.add("entering outer transformed test")
                test(fixture, descriptor)
                log.add("leaving outer transformed test")
            }
        }

        val innerTransform: TestTransform<Unit> = { test ->
            { fixture: Unit, descriptor: TestDescriptor ->
                log.add("entering inner transformed test")
                test(fixture, descriptor)
                log.add("leaving inner transformed test")
            }
        }

        executeTests(rootContext<Unit> {

            addTransform(outerTransform)
            before { log.add("before outer") }
            after { log.add("after outer") }

            context("inner") {
                addTransform(innerTransform)
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
    
    @JUnitTest
    fun `transforms can disable tests`() {
        val log = mutableListOf<String>()
        val transform: TestTransform<Unit> = {
            { _: Unit, _: TestDescriptor -> /* no op */ }
        }

        executeTests(rootContext<Unit> {
            addTransform(transform)

            test("the test") { log.add("the test was invoked, but should not have been") }
        })
        
        assertNothingLogged(log)
    }
}