package dev.minutest.examples.experimental

import com.natpryce.hamkrest.*
import com.natpryce.hamkrest.assertion.assertThat
import dev.minutest.experimental.willRun
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.scenarios.*

private fun emptyMutableList() = mutableListOf<String>()

/**
 * Proof of concept for naming Then steps using Hamkrest descriptions
 */
class ScenariosHamkrestExampleTests : JUnit5Minutests {

    data class Fixture(
        val source: MutableList<String> = emptyMutableList(),
        val destination: MutableList<String> = emptyMutableList()
    )

    private val appleAndBanana = listOf("apple", "banana")

    fun tests() = rootContext<Fixture>("Moving Between Lists") {

        fixture {
            Fixture()
        }

        Scenario("Moving around items", elideTestNameAfterLength = 1000) {

            Given("an empty destination") {
                destination.clear()
            }.And("a source populated with $appleAndBanana") {
                source.addAll(appleAndBanana)
            }

            When("source moveInto destination") {
                source.moveInto(destination)
            }.ThenResult(equalTo(true))

            Then("destination", Fixture::destination, containsAll(listOf("apple", "banana")) and hasSize(equalTo(2)))
                .And("source", Fixture::source, isEmpty)

            When("moving back") {
                destination.moveInto(source)
            }.ThenResult(equalTo(true))

            And("fixture", { this },
                has(Fixture::source, containsAll(appleAndBanana) and hasSize(equalTo(2))) and
                    has(Fixture::destination, isEmpty))
        }

        willRun(
            "Moving Between Lists",
            "  Moving around items",
            "    Given an empty destination, And a source populated with [apple, banana]," +
                " When source moveInto destination," +
                " Then result is equal to true," +
                " Then destination contains all [\"apple\", \"banana\"] and has size that is equal to 2," +
                " And source is empty," +
                " When moving back," +
                " Then result is equal to true," +
                " And fixture has source that contains all [\"apple\", \"banana\"] and has size that is equal to 2 and has destination that is empty"
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

fun <F, R> Whens<F, R>.ThenResult(matcher: Matcher<R>): ResultingThens<F, R> = Then("result ${matcher.description}") {
    assertThat(it, matcher)
}

fun <T> MutableCollection<T>.moveInto(destination: MutableCollection<T>): Boolean =
    destination.addAll(this).also {
        clear()
    }