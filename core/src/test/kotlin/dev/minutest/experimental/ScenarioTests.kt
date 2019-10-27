package dev.minutest.experimental

import dev.minutest.assertLogged
import dev.minutest.executeTests
import dev.minutest.rootContext
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class ScenarioTests {

    val log = mutableListOf<String>()

    @Test
    fun `naming and execution`() {
        val tests = rootContext<String> {
            checkedAgainst(
                "root",
                "  Scenario",
                "    Given the string banana, When nothing happens, Then the fixture is banana, And it has 6 chars"
            )
            Scenario("Scenario") {
                GivenFixture("the string banana") {
                    "banana"
                }
                When("nothing happens") {
                    log += "in when"
                }
                Then("the fixture is banana") {
                    log += "in then"
                    assertEquals("banana", this)
                }
                And("it has 6 chars") {
                    log += "in and"
                    assertEquals(6, this.length)
                }
            }
        }
        executeTests(tests).orFail()
        assertLogged(log,
            "in when",
            "in then",
            "in and"
        )
    }

    @Test
    fun `GivenFixture And`() {
        val tests = rootContext<MutableList<String>> {
            Scenario("Scenario") {
                GivenFixture("an empty list") {
                    mutableListOf()
                }.And("banana in the list") {
                    log += "in and"
                    "banana".also {
                        this += it
                    }
                }.Then("it is banana") {
                    log += "in then"
                    assertEquals("banana", it)
                }
            }
        }
        executeTests(tests).orFail()
        assertLogged(log,
            "in and",
            "in then"
        )
    }

    @Test
    fun `GivenFixture passes fixture to Thens`() {
        val tests = rootContext<String> {
            Scenario("Scenario") {
                GivenFixture("the string banana") {
                    "banana"
                }.Then("it is banana") {
                    log += "in then"
                    assertEquals("banana", it)
                }.And("it has 6 chars") {
                    log += "in and"
                    assertEquals(6, it.length)
                    assertEquals(6, this.length)
                }.And("it begins with b") {
                    log += "in and"
                    assertTrue(it.startsWith("b"))
                }
            }
        }
        executeTests(tests).orFail()
        assertLogged(log,
            "in then",
            "in and",
            "in and"
        )
    }

    @Test
    fun `Given passes result to Thens`() {
        val tests = rootContext<MutableList<String>> {
            fixture { mutableListOf() }
            Scenario("Scenario") {
                Given("the string banana in the list") {
                    "banana".also {
                        fixture += it
                    }
                }.Then("it is banana") { it ->
                    log += "in then"
                    assertEquals("banana", it)
                    assertEquals(listOf("banana"), this)
                }.And("it has 6 chars") {
                    log += "in and"
                    assertEquals(6, it.length)
                    assertEquals(listOf("banana"), this)
                }
            }
        }
        executeTests(tests).orFail()
        assertLogged(log,
            "in then",
            "in and"
        )
    }

    @Test
    fun `When And`() {
        val tests = rootContext<MutableList<String>> {
            fixture { mutableListOf() }
            Scenario("Scenario") {
                GivenFixture("an empty list") {
                    mutableListOf()
                }
                When("add in an element") {
                    log += "in when"
                    "banana".also {
                        this += it
                    }
                }.And("add another") {
                    log += "in and"
                    "kumquat".also {
                        this += it
                    }
                }.Then("it is kumquat") { it ->
                    log += "in then"
                    assertEquals("kumquat", it)
                    assertEquals(listOf("banana", "kumquat"), this)
                }.And("it has 7 chars") {
                    log += "in and"
                    assertEquals(7, it.length)
                    assertEquals(listOf("banana", "kumquat"), this)
                }
            }
        }
        executeTests(tests).orFail()
        assertLogged(log,
            "in when",
            "in and",
            "in then",
            "in and"
        )
    }

    @Test
    fun `When passes result to Thens`() {
        val tests = rootContext<MutableList<String>> {
            fixture { mutableListOf() }
            Scenario("Scenario") {
                GivenFixture("an empty list") {
                    mutableListOf()
                }

                When("add in an element") {
                    "banana".also {
                        this += it
                    }
                }.Then("it is banana") { it ->
                    log += "in then"
                    assertEquals("banana", it)
                    assertEquals(listOf("banana"), this)
                }.And("it has 6 chars") {
                    log += "in and"
                    assertEquals(6, it.length)
                    assertEquals(listOf("banana"), this)
                }
            }
        }
        executeTests(tests).orFail()
        assertLogged(log,
            "in then",
            "in and"
        )
    }

    @Test
    fun `Standalone Then-And`() {
        val tests = rootContext<MutableList<String>> {
            fixture { mutableListOf() }
            Scenario("Scenario") {
                Then("it is empty") {
                    log += "in then"
                    assertTrue(fixture.isEmpty())
                }.And("its size is 0") {
                    log += "in and"
                    assertEquals(0, fixture.size)
                }
            }
        }
        executeTests(tests).orFail()
        assertLogged(log,
            "in then",
            "in and"
        )
    }
}

private fun List<Throwable>.orFail() = firstOrNull()?.let { throw it }