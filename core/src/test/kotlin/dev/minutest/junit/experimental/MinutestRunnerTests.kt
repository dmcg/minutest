package dev.minutest.junit.experimental

import dev.minutest.junit.JUnit5Minutests
import dev.minutest.junit.checkRunnersExampleLog
import dev.minutest.rootContext
import dev.minutest.runTestsInClass


class MinutestRunnerTests : JUnit5Minutests {

    fun tests() = rootContext<Unit> {
        test("MinutestRunner runs tests") {
            checkRunnersExampleLog(
                runTestsInClass("samples.runners.MinutestRunnersThing", MinutestTestEngine.engineId),
                "Minutest",
                "samples.runners",
                "tests")
        }
    }
}
