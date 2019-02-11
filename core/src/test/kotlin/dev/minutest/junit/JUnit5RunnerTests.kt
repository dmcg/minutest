package dev.minutest.junit

import dev.minutest.rootContext
import dev.minutest.runTestsInClass
import org.junit.jupiter.engine.JupiterTestEngine
import samples.runners.JUnit5RunnersThing


class JUnit5RunnerTests : JUnit5Minutests {

    fun tests() = rootContext<Unit> {
        test("JUnit 5 runs tests") {
            checkRunnersExampleLog(runTestsInClass<JUnit5RunnersThing>(JupiterTestEngine.ENGINE_ID),
                "JUnit Jupiter",
                "JUnit5RunnersThing",
                "minutests()")
        }
    }
}
