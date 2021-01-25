package dev.minutest.junit.experimental

import dev.minutest.junit.checkRunnersExampleLog
import dev.minutest.testing.runTestsInClass
import org.junit.jupiter.api.Test
import org.junit.vintage.engine.descriptor.VintageTestDescriptor
import samples.runners.JUnit4MultiRootSample
import samples.runners.JUnit4Sample
import samples.runners.expected
import samples.runners.multiRootExpected

class JUnit4RunnerTests {

    @Test fun singleRootTestsAndFailures() {
        checkRunnersExampleLog(
            runTestsInClass<JUnit4Sample>(VintageTestDescriptor.ENGINE_ID),
            substitutions = listOf(
                "ENGINE_NAME" to "JUnit Vintage",
                "TEST_NAME" to "JUnit4Sample",
                "CONTEXT_NAME" to "tests"
            ),
            hasExtraRoot = true,
            abortRatherThanSkip = false,
            expected = expected
        )
    }

    @Test fun multiRoot() {
        checkRunnersExampleLog(
            runTestsInClass<JUnit4MultiRootSample>(VintageTestDescriptor.ENGINE_ID),
            substitutions = listOf(
                "ENGINE_NAME" to "JUnit Vintage",
                "TEST_NAME" to "JUnit4MultiRootSample",
            ),
            hasExtraRoot = true,
            abortRatherThanSkip = true,
            expected = multiRootExpected
        )
    }
}
