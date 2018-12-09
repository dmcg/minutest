package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.JUnit5Minutests
import com.oneeyedmen.minutest.junit.context
import org.junit.jupiter.api.Assertions.assertEquals


class ParentFixtureExampleTests : JUnit5Minutests {

    data class Fixture(var fruit: String)

    override val tests = context<Fixture> {
        fixture {
            Fixture("banana")
        }

        test("sees the context's fixture") {
            assertEquals("banana", fruit)
        }

        context("context inherits fixture") {
            test("sees the parent context's fixture") {
                assertEquals("banana", fruit)
            }
        }

        context("context replaces fixture") {
            fixture {
                Fixture("kumquat")
            }
            test("sees the replaced fixture") {
                assertEquals("kumquat", fruit)
            }
        }

        context("context modifies fixture") {
            modifyFixture {
                fruit = "apple"
            }
            test("sees the modified fixture") {
                assertEquals("apple", fruit)
            }
        }
    }
}
