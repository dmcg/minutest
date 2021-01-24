package dev.minutest.examples.experimental

import dev.minutest.experimental.flatten
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import dev.minutest.test2
import org.junit.jupiter.api.Assertions.assertTrue

@Suppress("USELESS_IS_CHECK")

class FlatteningExampleTests : JUnit5Minutests {

    fun tests() = rootContext<Sequence<String>> {

        // parent supplies a sequence of fixtures
        fixture { listOf("one", "two", "three").asSequence() }

        // child requires a single fixture
        derivedContext<String>("flattened") {

            // flatten does the magic
            flatten()

            beforeEach {
                // will be invoked with each item in the sequence
            }

            test2("fixture is each item") {
                assertTrue(it is String)
            }

            test2("is not empty") {
                assertTrue(it.isNotEmpty())
            }

            test2("is two") {
                // would fail for most cases, collected into a MultipleFailuresError
                // assertEquals("two", this)
            }

            after {
                // will be invoked with each item in the sequence
            }
        }
    }
}
