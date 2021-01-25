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

    @Test fun junit4() {
        checkRunnersExampleLog(
            runTestsInClass<JUnit4Sample>(VintageTestDescriptor.ENGINE_ID),
            substitutions = listOf(
                "ENGINE_NAME" to "JUnit Vintage",
                "TEST_NAME" to "JUnit4Sample",
                "ROOT_NAME" to "junit 4 tests"
            ),
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
                "ROOT_NAME" to "samples.runners.JUnit4MultiRootSample"
            ),
            abortRatherThanSkip = true,
            expected = multiRootExpected
        )
    }
}
