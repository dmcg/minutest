package dev.minutest.examples.experimental

import dev.minutest.experimental.flatten
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import org.junit.jupiter.api.Assertions
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

            before {
                // will be invoked with each item in the sequence
            }

            test("fixture is each item") {
                assertTrue(this is String)
            }

            test("is not empty") {
                assertTrue(isNotEmpty())
            }

            test("is two") {
                // would fail for most cases, collected into a MultipleFailuresError
                // assertEquals("two", this)
            }

            after {
                // will be invoked with each item in the sequence
            }
        }
    }


    // TODO find a better place for me
    fun JUnitRulesExampleTests.additionalTestIsNotExecuted() = rootContext {
        test("should not be executed, or should it?"){
            Assertions.fail("was executed nonetheless")
        }
    }
}
