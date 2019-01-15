package com.oneeyedmen.minutest

import com.oneeyedmen.minutest.junit.JUnit5Minutests
import example.assumptions.JUnit4AssumptionsTest
import example.assumptions.JUnit5AssumptionsTest
import org.junit.jupiter.engine.JupiterTestEngine
import org.junit.vintage.engine.descriptor.VintageTestDescriptor


class AssumeTests : JUnit5Minutests {

    override val tests = rootContext<Unit> {
        context("JUnit 4") {
            test("with JUnit 4 assumptions") {
                checkLog(runTestsInClass<JUnit4AssumptionsTest>(VintageTestDescriptor.ENGINE_ID))
            }
        }
        context("JUnit 5") {
            test("with JUnit 5 assumptions") {
                checkLog(runTestsInClass<JUnit5AssumptionsTest>(JupiterTestEngine.ENGINE_ID))
            }
        }
    }

    private fun checkLog(log: List<String>) {
        assertThereIsALogItem(log) { it == "test aborted: should not be run" }
        assertThereIsALogItem(log) { it == "test aborted: assume in a test aborts it" }
    }
}
