package dev.minutest.examples.experimental

import dev.minutest.ContextBuilder
import dev.minutest.NodeBuilder
import dev.minutest.TestDescriptor
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions.assertTrue
import org.opentest4j.MultipleFailuresError


class MultiTestsExampleTests : JUnit5Minutests {

    fun tests() = rootContext<Sequence<String>> {

        fixture { listOf("one", "two", "three").asSequence() }

        tests("fixture is each item") {
            println(this)
            assertTrue(this is String)
        }

        tests("is not empty") {
            assertTrue(this.isNotEmpty())
        }

        tests_("is not empty and returns different fixture") {
            assertTrue(this.isNotEmpty())
            this + " done"
        }

        after {
            println(fixture.joinToString())
        }
    }
}

fun <F> ContextBuilder<Sequence<F>>.tests(name: String, f: F.(testDescriptor: TestDescriptor) -> Unit):
    NodeBuilder<Sequence<F>> = tests_(name) { testDescriptor ->
    this.apply {
        f(testDescriptor)
    }
}


fun <F> ContextBuilder<Sequence<F>>.tests_(name: String, f: F.(testDescriptor: TestDescriptor) -> F):
    NodeBuilder<Sequence<F>> =
    test_(name) { testDescriptor ->
        val failures = mutableListOf<Throwable>()
        fixture.map { aFixture ->
            try {
                aFixture.f(testDescriptor)
            } catch (t: Throwable) {
                failures.add(t)
                aFixture
            }

        }.toList().asSequence().also {
            // The issue is that here, if there is any error, we throw away all the mapped fixtures, and the caller
            // will just pass the previous Sequence<F> to after
            if (failures.isNotEmpty())
                throw MultipleFailuresError("Test $name for ", failures)
        }
    }