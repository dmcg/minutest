package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.JupiterTests
import org.junit.jupiter.api.Assertions.assertEquals


object DerivedContextExampleTests : JupiterTests {

    data class Fixture(val fruit: String)

    data class DerivedFixture(val fixture: Fixture, val thing: String)

    override val tests = context<Fixture> {

        fixture { Fixture("banana") }

        test("takes Fixture1") {
            assertEquals("banana", fruit)
        }

        derivedContext<DerivedFixture>("inner") {

            fixture {
                // here `this` is Fixture
                DerivedFixture(this, "smoothie")
            }

            test("takes Fixture 2") {
                assertEquals("banana", fixture.fruit)
                assertEquals("smoothie", thing)
            }
        }
    }
}
