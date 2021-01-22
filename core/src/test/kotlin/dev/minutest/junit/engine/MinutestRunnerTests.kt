package dev.minutest.junit.engine

import dev.minutest.junit.checkRunnersExampleLog
import dev.minutest.testing.runTestsInClass
import org.junit.jupiter.api.Test


class MinutestRunnerTests {

    @Test fun `tests as functions`() {
        checkRunnersExampleLog(
            runTestsInClass(MinutestTestEngine.engineId, "samples.runners.MinutestSample"),
            "Minutest",
            "samples.runners",
            "tests",
            abortRatherThanSkip = true
        )
    }

    @Test fun `tests in class`() {
        checkRunnersExampleLog(
            runTestsInClass(MinutestTestEngine.engineId, "samples.runners.MinutestClassSample"),
            "Minutest",
            "samples.runners.MinutestClassSample",
            "tests",
            abortRatherThanSkip = true
        )
    }
}
