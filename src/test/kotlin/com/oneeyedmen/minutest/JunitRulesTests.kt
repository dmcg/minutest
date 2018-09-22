package com.oneeyedmen.minutest

import org.junit.jupiter.api.TestFactory
import org.junit.rules.ExpectedException


object JunitRulesExampleTests {

    class Fixture {
        val expectedException = ExpectedException.none()
    }

    // theory here is that ExpectedException works, everything will!
    @TestFactory fun test() = context<Fixture>() {
        fixture {
            Fixture()
        }

        context("ExpectedException works") {
            before {
                expectedException.expectMessage("banana")
            }

            applyRule(Fixture::expectedException)

            test("exception is expected") {
                throw(RuntimeException("banana"))
            }
        }
    }
}
