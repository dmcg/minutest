package dev.minutest.scenarios

import dev.minutest.executeTests
import dev.minutest.experimental.checkedAgainst
import dev.minutest.experimental.noSymbolsLogger
import dev.minutest.rootContext
import org.junit.jupiter.api.Test


class ScenarioNamingTests {

    @Test
    fun `test name is generated from stages`() {
        val tests = rootContext<Unit> {
            checkedAgainst(
                "root",
                "  Scenario",
                "    Given given1, And given2, Then then1, When when1, And when2, Then then2, And then3, When when3, And when4, Then then4",
                logger = noSymbolsLogger()
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
    fun `Thens are elided if test name gets too long`() {
        val tests = rootContext<Unit> {
            checkedAgainst(
                "root",
                "  Scenario",
                "    Given given1, And given2, Then…, When when1, And when2, Then…, And…, When when3, And when4, Then…",
                logger = noSymbolsLogger()
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
                }.Then("then4 and some more characters") {
                }
            }
        }
        executeTests(tests).orFail()
    }

    @Test
    fun `test has scenario name if no givens (and hence no context)`() {
        val tests = rootContext<Unit> {
            checkedAgainst(
                "root",
                "  Scenario",
                logger = noSymbolsLogger()
            )
            Scenario("Scenario") {
                When("when") {}.Then("then") {}
            }
        }
        executeTests(tests).orFail()
    }

    @Test
    fun `will generate scenario name if none provided`() {
        // in this case no context is generated
        val tests = rootContext<Unit> {
            checkedAgainst(
                "root",
                "  When when, Then then",
                logger = noSymbolsLogger()
            )
            Scenario {
                When("when") {}.Then("then") {}
            }
        }
        executeTests(tests).orFail()
    }

    @Test
    fun `will use test for test description if none provided and has context and test`() {
        // in this case no context is generated
        val tests = rootContext<Unit> {
            checkedAgainst(
                "root",
                "  Given given, When when, Then then",
                "    test",
                logger = noSymbolsLogger()
            )
            Scenario {
                GivenFixture("given") {}
                When("when") {}.Then("then") {}
            }
        }
        executeTests(tests).orFail()
    }
}

private fun List<Throwable>.orFail() = firstOrNull()?.let { throw it }