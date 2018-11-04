package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.JupiterTests
import com.oneeyedmen.minutest.junit.context
import org.junit.jupiter.api.Assertions.assertEquals


object DerivedContextExampleTests : JupiterTests {

    data class Fixture(val fruit: String)

    data class DerivedFixture(val fixture: Fixture, val thing: String)

    override val tests = context<Fixture> {

        fixture { Fixture("banana") }

        test("takes Fixture") {
            assertEquals("banana", fruit)
        }

        derivedContext<DerivedFixture>("inner converting fixture later") {
            replaceFixture {
                // here `this` is Fixture
                DerivedFixture(this, "smoothie")
            }

            test("takes DerivedFixture") {
                assertEquals(DerivedFixture(Fixture("banana"), "smoothie"), this)
            }
        }

        derivedContext<DerivedFixture>("inner supplying converter", { DerivedFixture(this, "smoothie") }) {
            test("takes DerivedFixture") {
                assertEquals(DerivedFixture(Fixture("banana"), "smoothie"), this)
            }
        }

        derivedContext("inner supplying fixture", DerivedFixture(Fixture("apple"), "pie")) {
            test("takes DerivedFixture") {
                assertEquals(DerivedFixture(Fixture("apple"), "pie"), this)
            }
        }
    }
}
