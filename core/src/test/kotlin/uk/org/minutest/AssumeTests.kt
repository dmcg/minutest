package uk.org.minutest

import org.junit.jupiter.engine.JupiterTestEngine
import org.junit.vintage.engine.descriptor.VintageTestDescriptor
import samples.assumptions.JUnit4AssumptionsTest
import samples.assumptions.JUnit5AssumptionsTest
import uk.org.minutest.experimental.SKIP
import uk.org.minutest.junit.JUnit5Minutests


class AssumeTests : JUnit5Minutests {

    override val tests = rootContext<Unit> {
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
                checkLog(runTestsInClass<JUnit5AssumptionsTest>(JupiterTestEngine.ENGINE_ID))
            }
            // TODO
            SKIP - test("with JUnit 4 assumptions") {
                checkLog(runTestsInClass<JUnit4AssumptionsTest>(JupiterTestEngine.ENGINE_ID))
            }
        }
    }

    private fun checkLog(log: List<String>) {
        assertThereIsALogItem(log) { it == "test aborted: should not be run" }
        assertThereIsALogItem(log) { it == "test aborted: assume in a test aborts it" }
    }
}
