package dev.minutest

import dev.minutest.junit.JUnit5Minutests
import org.junit.jupiter.api.Assertions.assertEquals


class DerivedContextTests : JUnit5Minutests {

    data class Fixture(val fruit: String)

    data class DerivedFixture(val fixture: Fixture, val thing: String)

    fun tests() = rootContext<Fixture> {

        fixture { Fixture("banana") }

        test2("takes Fixture") {
            assertEquals("banana", fruit)
        }

        derivedContext<DerivedFixture>("inner converting fixture later") {

            deriveFixture {
                DerivedFixture(parentFixture, "smoothie")
            }

            test2("takes DerivedFixture") {
                assertEquals(DerivedFixture(Fixture("banana"), "smoothie"), this)
            }
        }
    }
}
