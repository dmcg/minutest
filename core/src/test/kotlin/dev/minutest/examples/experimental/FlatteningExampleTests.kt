package dev.minutest.examples.experimental

import dev.minutest.*
import dev.minutest.experimental.flatten
import dev.minutest.junit.JUnit5Minutests
import org.junit.jupiter.api.Assertions.assertTrue

@Suppress("USELESS_IS_CHECK")

class FlatteningExampleTests : JUnit5Minutests {

    fun tests() = rootContext<Sequence<String>> {

        // parent supplies a sequence of fixtures
        given { listOf("one", "two", "three").asSequence() }

        // child requires a single fixture
        derivedContext<String>("flattened") {

            // flatten does the magic
            flatten()

            beforeEach {
                // will be invoked with each item in the sequence
            }

            test("fixture is each item") {
                assertTrue(it is String)
            }

            test("is not empty") {
                assertTrue(it.isNotEmpty())
            }

            test("is two") {
                // would fail for most cases, collected into a MultipleFailuresError
                // assertEquals("two", this)
            }

            afterEach {
                // will be invoked with each item in the sequence
            }
        }
    }
}
