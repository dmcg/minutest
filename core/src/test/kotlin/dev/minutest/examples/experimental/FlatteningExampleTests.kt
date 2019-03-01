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

    // we want the nested context to get fixtures from the Sequence - this will be filled in later
    var fixtureValue: F? = null
    deriveFixture {
        fixtureValue ?: error("No fixture pulled from sequence")
    }

    annotateWith(object : TestAnnotation, NodeTransform {
        @Suppress("UNCHECKED_CAST")
        override fun <F2> applyTo(node: Node<F2>): Node<F2> {
            val context = (node as? Context<Sequence<F>, F>) ?: error("Not a context")
            fun runner(test: Testlet<F>, fixtures: Sequence<F>, testDescriptor: TestDescriptor): F {
                val primer = fixtures.onEach { fixtureValue = it }

                val failures = mutableListOf<Throwable>()
                return primer
                    .map { individualFixture ->
                        try {
                            context.runTest(test, primer, testDescriptor)
                        } catch (t: Throwable) {
                            failures.add(t)
                            individualFixture
                        }
                    }.last().also {
                        if (failures.isNotEmpty())
                            throw MultipleFailuresError("Test ${testDescriptor.name} for ", failures)
                    }
            }
            return ContextWrapper(context, runner = ::runner) as Node<F2>
        }
    })
}



