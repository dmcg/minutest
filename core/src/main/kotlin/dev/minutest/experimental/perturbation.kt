package dev.minutest.experimental

import dev.minutest.ContextBuilder
import dev.minutest.test


interface Scenario<R> {
    val baseResult: R
    fun setUp()
    fun evaluate(): R
}

fun <S : Scenario<*>> asserter(name: String, f: (S).() -> Unit) =
    object : (S) -> Unit by f {
        override fun toString() = name
    }

class PerturbationContext<R, S : Scenario<R>>(
    private val baseScenarioBuilder: () -> S,
    val baseAsserter: S.() -> Unit
) {

    constructor(
        baseScenarioBuilder: () -> S,
        assertEquals: (R, R) -> Unit,
    ) :
        this(
            baseScenarioBuilder = baseScenarioBuilder,
            baseAsserter = asserter("returns baseResult") {
                assertEquals(this.baseResult, this.evaluate())
            }
        )

    fun ContextBuilder<*>.baseTest() {
        mutationTest(
            mutation("no mutation") { it },
            baseAsserter
        )
    }

    fun ContextBuilder<*>.mutationTest(
        mutation: Mutation<S>,
        asserter: S.() -> Unit
    ) {
        test(
            Mutant(
                mutation.toString(),
                mutation(baseScenarioBuilder())
            ),
            asserter
        )
    }

    fun ContextBuilder<*>.mutationTests(
        mutations: Iterable<Mutation<S>>,
        asserterMapper: S.() -> S.() -> Unit
    ) {
        mutations.forEach { mutation ->
            val mutant = Mutant(
                mutation.toString(),
                mutation(baseScenarioBuilder())
            )
            test(
                mutant,
                asserterMapper(
                    mutant.value
                )
            )
        }
    }

    fun ContextBuilder<*>.partitionTests(
        mutations: Iterable<Mutation<S>>,
        partition: S.() -> Boolean,
        asserter: S.() -> Unit,
    ) {
        val mutants = mutations.map {
            Mutant(it.toString(), it(baseScenarioBuilder()))
        }
        val (potents, impotents) = mutants
            .partition { mutant -> partition(mutant.value) }
            .let { (potents, impotents) ->
                Pair(
                    potents.map { mutant -> mutant to asserter },
                    impotents.map { mutant -> mutant to baseAsserter }
                )
            }
        contextFor("potents", potents)
        contextFor("impotents", impotents)
    }

    private fun ContextBuilder<*>.test(
        mutant: Mutant<S>,
        asserter: S.() -> Unit
    ) {
        test("${mutant.name} -> $asserter") {
            with(mutant.value) {
                setUp()
                asserter(this)
            }
        }
    }

    private fun ContextBuilder<*>.contextFor(
        name: String,
        mutants: Iterable<Pair<Mutant<S>, (S) -> Unit>>
    ) {
        context(name) {
            mutants.forEach { (mutant, asserter) ->
                test(mutant, asserter)
            }
        }
    }
}