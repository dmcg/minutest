package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestFactory


object ChangeFixtureExampleTests {

    class BaseFixture(val thing: String)

    class AnotherFixture(val thingToo: String)

    @TestFactory
    fun `can swap fixtures`() = junitTests<BaseFixture> {

        fixture { BaseFixture("thing") }

        test("takes BaseFixture") {
            assertEquals("thing", thing)
        }

        // nested context
//        context<AnotherFixture>("") {
//
////            fixture { AnotherFixture(this.thing) }
//
//            test("takes AnotherFixture") {
//                assertEquals("thing", thingToo)
//            }
//        }
    }
}

