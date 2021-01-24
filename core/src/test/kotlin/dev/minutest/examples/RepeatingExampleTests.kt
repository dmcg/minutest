package dev.minutest.examples

import dev.minutest.experimental.willRun
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test2
import org.junit.jupiter.api.Assertions.assertEquals

class RepeatingExampleTests : JUnit5Minutests {

    fun tests() = rootContext {

        // Invoking the test block creates a test to be run later.
        // You can use plain old Kotlin to generate multiple otherwise identical tests.
        (1..3).forEach { count ->
            test2("check 1 is 1 - run $count") {
                assertEquals(1, 1)
            }
        }

        // This works for contexts too.
        (1..3).forEach { count ->
            context("context $count") {
                test2("check 2 is 2") {
                    assertEquals(2, 2)
                }
                test2("check 3 is 3") {
                    assertEquals(3, 3)
                }
            }
        }

        // Minutest will check that the following tests are run
        willRun(
            "▾ tests",
            "  ✓ check 1 is 1 - run 1",
            "  ✓ check 1 is 1 - run 2",
            "  ✓ check 1 is 1 - run 3",
            "  ▾ context 1",
            "    ✓ check 2 is 2",
            "    ✓ check 3 is 3",
            "  ▾ context 2",
            "    ✓ check 2 is 2",
            "    ✓ check 3 is 3",
            "  ▾ context 3",
            "    ✓ check 2 is 2",
            "    ✓ check 3 is 3"
        )
    }
}

