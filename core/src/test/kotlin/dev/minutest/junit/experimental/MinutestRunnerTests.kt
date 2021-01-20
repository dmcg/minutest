package dev.minutest.junit.experimental

import dev.minutest.junit.checkRunnersExampleLog
import dev.minutest.testing.runTestsInClass
import org.junit.jupiter.api.Test


class MinutestRunnerTests {

    @Test fun test() {
        checkRunnersExampleLog(
            runTestsInClass(MinutestTestEngine.engineId, "samples.runners.MinutestSample"),
            "Minutest",
            "samples.runners",
            "tests",
            abortRatherThanSkip = true
        )
    }
}
