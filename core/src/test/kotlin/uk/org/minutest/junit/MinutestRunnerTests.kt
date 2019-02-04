package uk.org.minutest.junit

import uk.org.minutest.rootContext
import uk.org.minutest.runTestsInClass


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
