package dev.minutest.experimental

import dev.minutest.ContextBuilder
import dev.minutest.test


interface TestParameters<R> {
    val baseResult: R
    fun setUp()
    fun evaluate(): R
}

fun <T : TestParameters<*>> asserter(name: String, f: (T).() -> Unit) =
    object : (T) -> Unit by f {
        override fun toString() = name
    }

class PerturbationContext<R, T : TestParameters<R>>(
    private val parametersBuilder: () -> T,
    val baseAsserter: T.() -> Unit
) {

    constructor(
        parametersBuilder: () -> T,
        assertEquals: (R, R) -> Unit,
    ) :
        this(
            parametersBuilder = parametersBuilder,
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
        mutation: Mutation<T>,
        asserter: T.() -> Unit
    ) {
        test(
            Mutant(
                mutation.toString(),
                mutation(parametersBuilder())
            ),
            asserter
        )
    }

    fun ContextBuilder<*>.mutationTests(
        mutations: Iterable<Mutation<T>>,
        asserterMapper: T.() -> T.() -> Unit
    ) {
        mutations.forEach { mutation ->
            val mutant = Mutant(
                mutation.toString(),
                mutation(parametersBuilder())
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
        mutations: Iterable<Mutation<T>>,
        partition: T.() -> Boolean,
        asserter: T.() -> Unit,
    ) {
        val mutants = mutations.map {
            Mutant(it.toString(), it(parametersBuilder()))
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
        mutant: Mutant<T>,
        asserter: T.() -> Unit
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
        mutants: Iterable<Pair<Mutant<T>, (T) -> Unit>>
    ) {
        context(name) {
            mutants.forEach { (mutant, asserter) ->
                test(mutant, asserter)
            }
        }
    }
}