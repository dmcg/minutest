package uk.org.minutest.junit

import org.junit.jupiter.engine.JupiterTestEngine
import samples.runners.JUnit5RunnersThing
import uk.org.minutest.rootContext
import uk.org.minutest.runTestsInClass


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
