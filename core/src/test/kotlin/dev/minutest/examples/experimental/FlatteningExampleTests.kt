package dev.minutest.examples.experimental

import dev.minutest.*
import dev.minutest.experimental.ContextWrapper
import dev.minutest.experimental.TestAnnotation
import dev.minutest.experimental.annotateWith
import dev.minutest.junit.JUnit5Minutests
import org.junit.jupiter.api.Assertions.assertTrue
import org.opentest4j.MultipleFailuresError

class FlatteningExampleTests : JUnit5Minutests {

    fun tests() = rootContext<Sequence<String>> {

        fixture { listOf("one", "two", "three").asSequence() }

        derivedContext<String>("flattened") {

            flatten()

            test("fixture is each item") {
                println(it.fullName() + this)
                assertTrue(this is String)
            }

            test("is not empty") {
                println(it.fullName() + this)
                assertTrue(isNotEmpty())
            }

            test_("is not empty and returns different fixture") {
                println(it.fullName() + this)
                assertTrue(isNotEmpty())
                this + " done"
            }

            after {
                println(fixture)
            }
        }
    }
}

fun <F> TestContextBuilder<Sequence<F>, F>.flatten() {

    deriveFixture {
        // 1 - By the time we get here, the parentFixture will contain just the one fixture we need for each test. See [2].
        parentFixture.first()
    }

    annotateWith(object : TestAnnotation, NodeTransform {
        @Suppress("UNCHECKED_CAST")
        override fun <F2> applyTo(node: Node<F2>): Node<F2> {
            val wrapped = (node as? Context<Sequence<F>, F>) ?: error("Not a context")
            return ContextWrapper(wrapped, runner = flatteningRunnerFor(wrapped)) as Node<F2>
        }
    })
}


private fun <F> flatteningRunnerFor(wrapped: Context<Sequence<F>, F>) =
    fun(test: Testlet<F>, fixtures: Sequence<F>, testDescriptor: TestDescriptor): F {
        val failures = mutableListOf<Throwable>()
        return fixtures
            .map { individualFixture ->
                try {
                    // 2 - Here we take the current fixture and make it the only thing in the parentFixture sequence - see [1]
                    wrapped.runTest(test, sequenceOf(individualFixture), testDescriptor)
                } catch (t: Throwable) {
                    failures.add(t)
                    individualFixture
                }
            }.last().also {
                if (failures.isNotEmpty())
                    throw MultipleFailuresError("Test ${testDescriptor.name} for ", failures)
            }
    }



