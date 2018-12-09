package com.oneeyedmen.minutest.examples

import com.oneeyedmen.minutest.junit.JUnit5Minutests
import com.oneeyedmen.minutest.junit.context
import org.junit.jupiter.api.Assertions.assertEquals


class DerivedContextExampleTests : JUnit5Minutests {

    data class Fruit(val name: String)

    data class FruitDrink(val fruit: Fruit, val name: String) {
        override fun toString() = "${fruit.name} $name"
    }

    override val tests = context<Fruit> {

        fixture {
            Fruit("banana")
        }

        test("takes Fixture") {
            assertEquals("banana", name)
        }

        // To change fixture type use derivedContext
        derivedContext<FruitDrink>("change in fixture type") {

            // We have to specify how to convert a Fruit to a FruitDrink
            deriveFixture {
                FruitDrink(parentFixture, "smoothie")
            }

            test("takes FruitDrink") {
                assertEquals("banana smoothie", this.toString())
            }
        }
    }
}
