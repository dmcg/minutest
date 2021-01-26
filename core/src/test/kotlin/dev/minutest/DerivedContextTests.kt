package dev.minutest

import dev.minutest.junit.JUnit5Minutests
import org.junit.jupiter.api.Assertions.assertEquals


class DerivedContextTests : JUnit5Minutests {

    data class Fixture(val fruit: String)

    data class DerivedFixture(val fixture: Fixture, val thing: String)

    fun tests() = rootContext<Fixture> {

        given { Fixture("banana") }

        test("takes Fixture") {
            assertEquals("banana", fruit)
        }

        derivedContext<DerivedFixture>("inner converting fixture later") {

            given_ { parentFixture ->
                DerivedFixture(parentFixture, "smoothie")
            }

            test("takes DerivedFixture") {
                assertEquals(DerivedFixture(Fixture("banana"), "smoothie"), this)
            }
        }
    }
}
