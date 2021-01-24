package dev.minutest

import dev.minutest.junit.JUnit5Minutests
import org.junit.jupiter.api.Assertions.assertEquals


class DynamicTests : JUnit5Minutests {

    data class Fixture(
        var fruit: String,
        val log: MutableList<String> = mutableListOf()
    )

    fun tests() = rootContext<Fixture> {
        given { Fixture("banana") }

        context("same fixture for each") {
            (1..3).forEach { i ->
                test2("test for $i") {}
            }
        }

        context("modify fixture for each test") {
            (1..3).forEach { i ->
                context("banana count $i") {
                    given_ { parentFixture ->  Fixture("$i ${parentFixture.fruit}") }
                    test2("test for $i") {
                        assertEquals("$i banana", fruit)
                    }
                }
            }
        }
    }
}