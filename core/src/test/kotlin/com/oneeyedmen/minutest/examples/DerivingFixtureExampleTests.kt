package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.derivedContext
import com.oneeyedmen.minutest.junit.junitTests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestFactory


object DerivingFixtureExampleTests {

    data class Fixture(val fruit: String)

    data class DerivedFixture(val fixture: Fixture, val thing: String)

    @TestFactory fun test() = junitTests<Fixture> {

        fixture { Fixture("banana") }

        test("takes Fixture1") {
            assertEquals("banana", fruit)
        }

        derivedContext<Fixture, DerivedFixture>("inner") {

            // Not valid as we need to supply a DerivedFixture
            // modifyFixture {}
            // replaceFixture {}

            deriveFixture {
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
