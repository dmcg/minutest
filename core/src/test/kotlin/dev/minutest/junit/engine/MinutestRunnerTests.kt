package dev.minutest.junit.engine

import dev.minutest.junit.checkRunnersExampleLog
import dev.minutest.testing.runTestsInClass
import org.junit.jupiter.api.Test
import samples.runners.MinutestClassSample
import samples.runners.MinutestMultiRootClassSample
import samples.runners.expected
import samples.runners.multiRootExpected


class MinutestRunnerTests {

    @Test fun `tests as functions`() {
        checkRunnersExampleLog(
            runTestsInClass(MinutestTestEngine.engineId, "samples.runners.MinutestSample"),
            substitutions = listOf(
                "ENGINE_NAME" to "Minutest",
                "TEST_NAME" to "samples.runners",
                "CONTEXT_NAME" to "tests"
            ),
            hasExtraRoot = true,
            abortRatherThanSkip = true,
            expected = expected
        )
    }

    @Test fun `tests in class`() {
        checkRunnersExampleLog(
            runTestsInClass<MinutestClassSample>(MinutestTestEngine.engineId),
            substitutions = listOf(
                "ENGINE_NAME" to "Minutest",
                "TEST_NAME" to "samples.runners.MinutestClassSample",
                "CONTEXT_NAME" to "tests"
            ),
            hasExtraRoot = true,
            abortRatherThanSkip = true,
            expected = expected
        )
    }

    @Test fun multiRoot() {
        checkRunnersExampleLog(
            runTestsInClass<MinutestMultiRootClassSample>(MinutestTestEngine.engineId),
            substitutions = listOf(
                "ENGINE_NAME" to "Minutest",
                "TEST_NAME" to "samples.runners.MinutestMultiRootClassSample",
            ),
            hasExtraRoot = true,
            abortRatherThanSkip = true,
            expected = multiRootExpected
        )
    }
}
