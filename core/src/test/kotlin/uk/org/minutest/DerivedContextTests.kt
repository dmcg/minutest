package uk.org.minutest

import org.junit.jupiter.api.Assertions.assertEquals
import uk.org.minutest.junit.JUnit5Minutests


class DerivedContextTests : JUnit5Minutests {

    data class Fixture(val fruit: String)

    data class DerivedFixture(val fixture: Fixture, val thing: String)

    override val tests = rootContext<Fixture> {

        fixture { Fixture("banana") }

        test("takes Fixture") {
            assertEquals("banana", fruit)
        }

        derivedContext<DerivedFixture>("inner converting fixture later") {

            deriveFixture {
                DerivedFixture(parentFixture, "smoothie")
            }

            test("takes DerivedFixture") {
                assertEquals(DerivedFixture(Fixture("banana"), "smoothie"), this)
            }
        }
    }
}
