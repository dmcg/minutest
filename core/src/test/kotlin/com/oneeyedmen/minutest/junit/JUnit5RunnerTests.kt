package com.oneeyedmen.minutest.junit

import com.oneeyedmen.minutest.rootContext
import com.oneeyedmen.minutest.runTestsInClass
import example.runners.JUnit5RunnersThing
import example.runners.checkRunnersExampleLog
import org.junit.jupiter.engine.JupiterTestEngine


class JUnit5RunnerTests : JUnit5Minutests {

    override val tests = rootContext<Unit> {
        test("JUnit 5 runs tests") {
            checkRunnersExampleLog(runTestsInClass<JUnit5RunnersThing>(JupiterTestEngine.ENGINE_ID),
                "JUnit Jupiter",
                "JUnit5RunnersThing",
                "tests()")
        }
    }
}
