package dev.minutest.experimental

import dev.minutest.executeTests
import dev.minutest.rootContext
import org.junit.jupiter.api.Test


class ScenarioNamingTests {

    @Test
    fun `Name is Givens and Whens with elided Thens`() {
        val tests = rootContext<Unit> {
            checkedAgainst(
                "root",
                "  Scenario",
                "    Given given1, And given2, Then…, When when1, And when2, Then…, And…, When when3, And when4, Then…"
            )
            Scenario("Scenario") {
                GivenFixture("given1") {
                }.And("given2") {
                }.Then("then1") {}
                When("when1") {}
                And("when2") {}
                Then("then2") {}
                And("then3") {}
                When("when3") {
                }.And("when4") {
                }.Then("then4") {
                }
            }
        }
        executeTests(tests).orFail()
    }

    @Test
    fun `Survives no Givens or Whens`() {
        val tests = rootContext<Unit> {
            checkedAgainst(
                "root",
                "  Scenario",
                "    Then…, And…"
            )
            Scenario("Scenario") {
                Then("then1") {}
                And("then2") {}
            }
        }
        executeTests(tests).orFail()
    }
}

private fun List<Throwable>.orFail() = firstOrNull()?.let { throw it }