package dev.minutest.internal

import dev.minutest.TestDescriptor
import dev.minutest.Testlet
import org.opentest4j.TestAbortedException

// little hack to allow JUnit 4 assumptions
fun <F> Testlet<F>.translatingAssumptions(): Testlet<F> =
    { f: F, testDescriptor: TestDescriptor ->
        try {
            this(f, testDescriptor)
        } catch (x: RuntimeException) {
            throw translateAssumptions(x)
        }
    }

private fun translateAssumptions(x: RuntimeException) =
    when (x::class.qualifiedName) {
        // by name so that we don't need the jar file
        "org.junit.AssumptionViolatedException" -> TestAbortedException(x.message)
        else -> x
    }

