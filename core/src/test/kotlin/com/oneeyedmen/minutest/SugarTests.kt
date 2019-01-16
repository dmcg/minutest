package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.JUnit5Minutests
import org.junit.jupiter.api.Assertions.assertEquals


class SugarTests : JUnit5Minutests {

    data class Fruit(val name: String)
    data class Conserve(val type: String, val fruit: Fruit)

    override val tests = rootContext<Fruit> {

        fixture {
            Fruit("blackcurrent")
        }

        derivedContext<Conserve>("inner") {

            deriveFixture {
                assertEquals("blackcurrent", this.name)
                assertEquals("blackcurrent", parentFixture.name)
                // Doesn't compile
                // assertEquals("blackcurrent", fixture.name)
                Conserve("jam", parentFixture)
            }

            test("test") {
                assertEquals("jam", this.type)
                assertEquals("jam", fixture.type)
                // Doesn't compile
                // assertEquals("blackcurrent jam", parentFixture.fruit)
            }
        }
    }
}