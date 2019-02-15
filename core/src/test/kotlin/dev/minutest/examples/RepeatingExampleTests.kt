package dev.minutest.examples

import dev.minutest.experimental.checkedAgainst
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertEquals


class RepeatingExampleTests : JUnit5Minutests {

    fun tests() = rootContext<Unit>(checkedAgainst { assertEquals(summary, it) }) {

        // Invoking the test block creates a test to be run later.
        // You can use plain old Kotlin to generate multiple otherwise identical tests.
        (1 .. 3).forEach { count ->
            test("check 1 is 1 - run $count") {
                assertEquals(1, 1)
            }
        }

        // This works for contexts too.
        (1 .. 3).forEach { count ->
            context("context $count") {
                test("check 2 is 2") {
                    assertEquals(2, 2)
                }
                test("check 3 is 3") {
                    assertEquals(3, 3)
                }
            }
        }
    }

    val summary = listOf(
        "root",
        "  check 1 is 1 - run 1",
        "  check 1 is 1 - run 2",
        "  check 1 is 1 - run 3",
        "  context 1",
        "    check 2 is 2",
        "    check 3 is 3",
        "  context 2",
        "    check 2 is 2",
        "    check 3 is 3",
        "  context 3",
        "    check 2 is 2",
        "    check 3 is 3"
    )
}

