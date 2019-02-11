package dev.minutest

import dev.minutest.experimental.SKIP
import dev.minutest.junit.JUnit5Minutests
import org.junit.vintage.engine.descriptor.VintageTestDescriptor
import samples.assumptions.JUnit4AssumptionsTest
import samples.assumptions.JUnit5AssumptionsTest


class AssumeTests : JUnit5Minutests {

    fun tests() = rootContext<Unit> {
        context("JUnit 4") {
            test("with JUnit 4 assumptions") {
                checkLog(runTestsInClass<JUnit4AssumptionsTest>(VintageTestDescriptor.ENGINE_ID))
            }
            // TODO
            SKIP - test("with JUnit 5 assumptions") {
                checkLog(runTestsInClass<JUnit5AssumptionsTest>(VintageTestDescriptor.ENGINE_ID))
            }
        }
        context("JUnit 5") {
            test("with JUnit 5 assumptions") {
                checkLog(runTestsInClass<JUnit5AssumptionsTest>("junit-jupiter"))
            }
            // TODO
            SKIP - test("with JUnit 4 assumptions") {
                checkLog(runTestsInClass<JUnit4AssumptionsTest>("junit-jupiter"))
            }
        }
    }

    private fun checkLog(log: List<String>) {
        assertThereIsALogItem(log) { it == "test aborted: should not be run" }
        assertThereIsALogItem(log) { it == "test aborted: assume in a test aborts it" }
    }
}
