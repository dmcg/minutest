package dev.minutest.examples.experimental

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import dev.minutest.experimental.*
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.experimental.checkedAgainst as willRun

private fun emptyMutableList() = mutableListOf<String>()

class ScenariosHamkrestExampleTests : JUnit5Minutests {

    data class Fixture(
        val source: MutableList<String> = emptyMutableList(),
        val destination: MutableList<String> = emptyMutableList()
    )

    // A feature is a context
    fun tests() = rootContext<Fixture>("Moving Between Lists") {

        // we can populate the fixture as usual
        fixture {
            Fixture()
        }

        // Scenario defines a nested context
        Scenario("Moving around items") {

            // Given sets up the fixture for the scenario
            Given("an empty destination") {
                destination.clear()
            }.And("a populated source") {
                source.addAll(listOf("apple", "banana"))
            }

            // When is for operations
            When("source moveInto destination") {
                source.moveInto(destination)
            }

            // Then is for checks
            Then("destination", Fixture::destination, containsAll(listOf("apple", "banana")) and hasSize(equalTo(2)))
                .And("source", Fixture::source, isEmpty)

            // You can have more Whens
            When("moving back") {
                destination.moveInto(source)
            }.ThenResult(equalTo(true))

            Then("fixture", { this },
                has(Fixture::source, containsAll(listOf("apple", "banana")) and hasSize(equalTo(2))) and
                has(Fixture::destination, isEmpty))

        }


        // Minutest will check that the following tests are run - note that it is one long test name
        willRun(
            "Moving Between Lists",
            "  Moving around items",
            "    Given an empty destination, And a populated source," +
                " When source moveInto destination," +
                " When moving back"
        )
    }

}

private fun <T> containsAll(cmp: List<T>) = Matcher(Collection<T>::containsAll, cmp)

fun <F, R> ScenarioBuilder<F>.Then(thing: String, f: F.() -> R, matcher: Matcher<R>): Thens<F> =
    Then("$thing ${matcher.description}") {
        assertThat(f(this), matcher)
    }

fun <F, R> ScenarioBuilder<F>.And(thing: String, f: F.() -> R, matcher: Matcher<R>): Unit =
    And("$thing ${matcher.description}") {
        assertThat(f(this), matcher)
    }

fun <F, R> Thens<F>.And(thing: String, f: F.() -> R, matcher: Matcher<R>) = And("$thing ${matcher.description}") {
    assertThat(f(this), matcher)
}

fun <F, R> Whens<F, R>.ThenResult( matcher: Matcher<R>): ResultingThens<F, R> = Then("result ${matcher.description}") {
    assertThat(it, matcher)
}