package dev.minutest

import dev.minutest.junit.JUnit5Minutests
import org.junit.jupiter.api.Assertions.assertEquals


class DynamicTests : JUnit5Minutests {

    data class Fixture(
        var fruit: String,
        val log: MutableList<String> = mutableListOf()
    )

    fun tests() = rootContext<Fixture> {
        fixture { Fixture("banana") }

        context("same fixture for each") {
            (1..3).forEach { i ->
                test("test for $i") {}
            }
        }

        context("modify fixture for each test") {
            (1..3).forEach { i ->
                context("banana count $i") {
                    deriveFixture { Fixture("$i ${fruit}") }
                    test("test for $i") {
                        assertEquals("$i banana", fruit)
                    }
                }
            }
        }
    }
}