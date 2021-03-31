package dev.minutest.examples

import dev.minutest.given
import dev.minutest.given_
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test
import org.junit.jupiter.api.Assertions.assertTrue


// You can change the fixture type as you go down the context tree.
@Suppress("USELESS_IS_CHECK")
class DerivedContextExampleTests : JUnit5Minutests {

    // Fruit and FruitDrink are our 2 fixture types

    data class Fruit(val name: String)

    data class FruitDrink(val fruit: Fruit, val name: String) {
        override fun toString() = "${fruit.name} $name"
    }

    // Our root fixture type is Fruit
    fun tests() = rootContext<Fruit>("Fruit Context") {

        given {
            Fruit("banana")
        }

        test("takes Fruit") {
            assertTrue(it is Fruit)
        }

        // To change fixture type use context_
        context_<FruitDrink>("FruitDrink Context") {

            // deriveFixture specifies how to convert a Fruit to a FruitDrink
            given_ { parentFixture ->
                FruitDrink(parentFixture, "smoothie")
            }

            test("takes FruitDrink") {
                assertTrue(it is FruitDrink)
            }

            // If you don't need access to the parent fixture, this would do
            // given {
            //     FruitDrink(Fruit("kumquat"), "milkshake")
            // }
        }
    }
}
