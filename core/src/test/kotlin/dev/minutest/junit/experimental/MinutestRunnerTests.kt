package dev.minutest.junit.experimental

import dev.minutest.junit.checkRunnersExampleLog
import dev.minutest.rootContext
import dev.minutest.runTestsInClass
import org.junit.jupiter.api.Test


class MinutestRunnerTests {

    @Test fun test() = rootContext<Unit> {
        checkRunnersExampleLog(
            runTestsInClass("samples.runners.MinutestSample", MinutestTestEngine.engineId),
            "Minutest",
            "samples.runners",
            "tests")
    }
}
