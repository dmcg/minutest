package dev.minutest

import dev.minutest.experimental.willRun
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.junit.experimental.JUnit4Minutests
import org.junit.Assert
import org.junit.Assume
import org.junit.jupiter.api.Assumptions


private fun assumptionsContract(assumer: (Boolean) -> Unit) =
    rootContext("assumptions skip tests") {

        test("assume in a test aborts it") {
            assumer(false)
            Assert.fail("shouldn't get here")
        }

        context("a context with assume in a fixture block") {
            modifyFixture {
                assumer(false)
            }
            test("should not be run") {
                Assert.fail("shouldn't get here")
            }
        }


        test("plain test") {
        }

        willRun(
            "▾ assumptions skip tests",
            "  - assume in a test aborts it",
            "  ▾ a context with assume in a fixture block",
            "  ✓ plain test"
        )
    }

class AssumptionsTests5 : JUnit5Minutests {
    fun junit4() = assumptionsContract(Assume::assumeTrue)

    fun junit5() = assumptionsContract(Assumptions::assumeTrue)
}

class AssumptionsTests4 : JUnit4Minutests() {
    fun junit4() = assumptionsContract(Assume::assumeTrue)

    fun junit5() = assumptionsContract(Assumptions::assumeTrue)
}

class AssumptionsTestsX {
    // TODO - can't run these until runner bug fixed
    fun junit4() = assumptionsContract(Assume::assumeTrue)

    fun junit5() = assumptionsContract(Assumptions::assumeTrue)
}