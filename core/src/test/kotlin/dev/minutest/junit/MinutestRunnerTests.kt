package dev.minutest.junit

import dev.minutest.rootContext
import dev.minutest.runTestsInClass


class MinutestRunnerTests : JUnit5Minutests {

    override val tests = rootContext<Unit> {
        test("MinutestRunner runs tests") {
            checkRunnersExampleLog(
                runTestsInClass("samples.runners.MinutestRunnersThing", MinutestTestEngine.engineId),
                "Minutest",
                "samples.runners",
                "tests")
        }
    }
}
