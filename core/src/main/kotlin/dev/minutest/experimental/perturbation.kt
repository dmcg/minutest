package dev.minutest.experimental

import dev.minutest.ContextBuilder
import dev.minutest.internal.duplicate
import dev.minutest.test
import kotlin.reflect.KProperty1


interface Scenario<R> {
    val baseResult: R
    fun setUp()
    fun evaluate(): R
    val asserter: (Scenario<R>) -> Unit
}

fun <S : Scenario<*>> asserter(name: String, f: (S).() -> Unit) =
    object : (S) -> Unit by f {
        override fun toString() = name
    }

fun <R, S: Scenario<R>> ContextBuilder<*>.baseTest(
    baseScenarioBuilder: () -> S
) {
    val scenario = baseScenarioBuilder()
    val mutant = Mutant("base", scenario)
        .withAsserter(scenario.asserter)
    test(mutant)
}

fun <R, S : Scenario<R>> ContextBuilder<*>.mutantTest(
    baseScenarioBuilder: () -> S,
    mutation: Mutation<S>,
    asserter: S.() -> Unit
) {
    val mutant = Mutant(
        mutation.toString(),
        mutation(baseScenarioBuilder())
    ).withAsserter(asserter)
    test(mutant)
}


fun <R, S : Scenario<R>> ContextBuilder<*>.trials(
    baseScenarioBuilder: () -> S,
    mutations: List<Mutation<S>>,
    asserter: S.() -> Unit
) {
    mutations.forEach { mutation ->
        mutantTest(
            baseScenarioBuilder = baseScenarioBuilder,
            mutation = mutation,
            asserter = asserter
        )
    }
}

fun <R, S : Scenario<R>> ContextBuilder<*>.partitionTests(
    baseScenarioBuilder: () -> S,
    mutations: List<Mutation<S>>,
    partition: S.() -> Boolean,
    asserter: S.() -> Unit
) {
    val mutants = mutations.map {
        Mutant(it.toString(), it(baseScenarioBuilder()))
    }
    val (potents, impotents) = mutants
        .partition { mutant -> partition(mutant.value) }
        .let { (potents, impotents) ->
            Pair(
                potents.map { mutant -> mutant.withAsserter(asserter) },
                impotents.map { mutant -> mutant.withAsserter(mutant.value.asserter) }
            )
        }
    contextFor("potents", potents)
    contextFor("impotents", impotents)
}

private fun <R, S : Scenario<R>> ContextBuilder<*>.contextFor(
    name: String,
    mutants: Iterable<Mutant<S>>
) {
    context(name) {
        mutants.forEach { mutant ->
            test(mutant)
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun <R, S : Scenario<R>> Mutant<S>.withAsserter(
    asserter: S.() -> Unit
): Mutant<S> {
    val asserterMutation = mutation<S>(asserter.toString()) { it.withAsserter(asserter) }
    return Mutant("$name -> $asserterMutation", asserterMutation(value))
}

@Suppress("UNCHECKED_CAST")
private fun <S: Scenario<*>> S.withAsserter(value: (S) -> Unit): S =
    this.duplicate(
        Scenario<*>::asserter as KProperty1<S, (S) -> Unit>,
        value
    )

private fun <R, S : Scenario<R>> ContextBuilder<*>.test(mutant: Mutant<S>) {
    test(mutant.name) {
        with(mutant.value) {
            setUp()
            asserter(this)
        }
    }
}