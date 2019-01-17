package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.rootContext
import com.oneeyedmen.minutest.runTestsInClass


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
