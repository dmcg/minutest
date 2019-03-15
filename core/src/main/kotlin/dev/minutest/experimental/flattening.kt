package dev.minutest.experimental

import dev.minutest.*
import org.opentest4j.MultipleFailuresError

/**
 * Invoke in a derivedContext to flatten a parent fixture of Sequence<F> to an F.
 */
fun <F> TestContextBuilder<Sequence<F>, F>.flatten() {

    deriveFixture {
        // 1 - By the time we get here, the parentFixture will contain just the one fixture we need for each test. See [2].
        parentFixture.first()
    }

    annotateWith(TransformingAnnotation { node: Node<Sequence<F>> ->
        @Suppress("UNCHECKED_CAST")
        val wrapped = (node as? Context<Sequence<F>, F>) ?: error("Not a context")
        ContextWrapper(wrapped, runner = flatteningRunnerFor(wrapped))
    })
}


private fun <F> flatteningRunnerFor(wrapped: Context<Sequence<F>, F>) =
    fun(test: Testlet<F>, fixtures: Sequence<F>, testDescriptor: TestDescriptor): F {
        val fixturesAndErrors: List<Pair<F, Throwable?>> = fixtures
            .map { individualFixture ->
                try {
                    // 2 - Here we take the current fixture and make it the only thing in the parentFixture sequence - see [1]
                    wrapped.runTest(test, sequenceOf(individualFixture), testDescriptor) to null
                } catch (t: Throwable) {
                    individualFixture to t
                }
            }.toList()
        val errors: List<Throwable> = fixturesAndErrors.mapNotNull { it.second }
        if (!errors.isEmpty())
            throw MultipleFailuresError("Test ${testDescriptor.name} for ", errors.toList())
        else
            return fixturesAndErrors.lastOrNull()?.first
                ?: error("There were unexpectedly no tests run - please report")
    }


